package org.drugis.trialverse.dataset.repository.impl;

import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

/**
 * Created by daan on 7-11-14.
 */
@Repository
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  private static final Logger logger = LoggerFactory.getLogger(DatasetReadRepositoryImpl.class);
  private static final String CONTAINS_STUDY_WITH_SHORTNAME = loadResource("askContainsStudyWithLabel.sparql");

  private static final Node CLASS_VOID_DATASET = NodeFactory.createURI("http://rdfs.org/ns/void#Dataset");
  private static final String HTTP_DRUGIS_ORG_EVENT_SOURCING_ES = "http://drugis.org/eventSourcing/es#";


  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private WebConstants webConstants;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private HttpClient httpClient;

  @Inject
  private AccountRepository accountRepository;

  private final Node headProperty = ResourceFactory.createProperty(HTTP_DRUGIS_ORG_EVENT_SOURCING_ES, "head").asNode();

  private enum FUSEKI_OUTPUT_TYPES {
    TEXT, JSON;

    @Override
    public String toString() {
      switch (this) {
        case TEXT:
          return "text";
        case JSON:
          return "json";
        default:
          throw new EnumConstantNotPresentException(FUSEKI_OUTPUT_TYPES.class, "nonexistent enum constant");
      }
    }
  }

  private static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("could not load resource " + filename);
  }


  private Boolean doAskQuery(URI trialverseDatasetUri, String query) {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    ResponseEntity<JsonObject> responseEntity = doRequest(versionMapping, query, RDFLanguages.JSONLD.getContentType().getContentType(), JsonObject.class);
    JsonObject jsonObject = responseEntity.getBody();
    return Boolean.TRUE.equals(Boolean.valueOf(jsonObject.get("boolean").toString()));
  }

  private <T> ResponseEntity<T> doRequest(VersionMapping versionMapping, String query, String acceptType, Class responseType) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(CONTENT_TYPE, WebContent.contentTypeSPARQLQuery);
    httpHeaders.add(ACCEPT, acceptType);

    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path(WebConstants.QUERY_ENDPOINT)
            .queryParam(WebConstants.QUERY_PARAM_QUERY, query)
            .build();

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, requestEntity, responseType);
  }

  @Override
  public Model queryDatasets(Account currentUserAccount) {
    List<VersionMapping> mappings = versionMappingRepository.findMappingsByEmail(currentUserAccount.getEmail());
    Model resultModel = ModelFactory.createDefaultModel();
    mappings.stream()
            .map(this::queryDataset)
            .forEach(resultModel::add);

    return resultModel;
  }

  @Override
  public Model queryDataset(VersionMapping mapping) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    String uri = mapping.getVersionedDatasetUrl() + WebConstants.DATA_ENDPOINT + WebConstants.QUERY_STRING_DEFAULT_GRAPH;

    ResponseEntity<Graph> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    final String version  = responseEntity.getHeaders().get(WebConstants.X_EVENT_SOURCE_VERSION).get(0);
    Graph datasetGraph = responseEntity.getBody();
    if (datasetGraph == null) { // can happen if the default graph is empty
      datasetGraph = GraphFactory.createGraphMem();
    }

    String datasetUri = getDatasetUri(datasetGraph);

    datasetGraph.add(new Triple(NodeFactory.createURI(datasetUri), headProperty, NodeFactory.createURI(version)));
    datasetGraph.add(new Triple(NodeFactory.createURI(datasetUri), RDF.Nodes.type, CLASS_VOID_DATASET));
    return  ModelFactory.createModelForGraph(datasetGraph);
  }


  @Override
  @Cacheable(cacheNames="versionedDataset", key="#trialverseDatasetUri.toString()+(#versionUuid?:'headVersion')")
  public Model getVersionedDataset(URI trialverseDatasetUri, String versionUuid) {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    if(StringUtils.isNotEmpty(versionUuid)) {
      httpHeaders.add(WebConstants.X_ACCEPT_EVENT_SOURCE_VERSION, WebConstants.buildVersionUri(versionUuid).toString());
    }
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    String uri = versionMapping.getVersionedDatasetUrl() + WebConstants.DATA_ENDPOINT + WebConstants.QUERY_STRING_DEFAULT_GRAPH;

    Graph datasetGraph = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class).getBody();
    if (datasetGraph == null) { // can happen if the default graph is empty
      datasetGraph = GraphFactory.createGraphMem();
    }
    String datasetUri = getDatasetUri(datasetGraph);

    Graph typedGraph = addDatasetType(datasetUri, datasetGraph);
    Graph graph = addCreator(datasetUri, versionMapping.getOwnerUuid(), typedGraph);
    return ModelFactory.createModelForGraph(graph);
  }

  @Override
  @Cacheable(cacheNames="versionedDatasetQuery", key="#trialverseDatasetUri.toString()+(#versionUri != null?#versionUri.toString():'headVersion')+#query.hashCode()")
  public byte[] executeQuery(String query, URI trialverseDatasetUri, URI versionUri, String acceptHeaderValue) throws IOException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path(WebConstants.QUERY_ENDPOINT)
            .queryParam(WebConstants.QUERY_PARAM_QUERY, URLEncoder.encode(query, "UTF-8")) // built-in encoding is too permissive
            .build(true);

    HttpGet request = new HttpGet(uriComponents.toUri());
    if(versionUri != null) {
      request.addHeader(WebConstants.X_ACCEPT_EVENT_SOURCE_VERSION, versionUri.toString());
    }
    request.addHeader(org.apache.http.HttpHeaders.ACCEPT, acceptHeaderValue);
    return executeRequestAndCloseResponse(request);
  }

  @Override
  public Boolean isOwner(URI trialverseDatasetUri, Principal principal) {
    TrialversePrincipal tvPrincipal = new TrialversePrincipal(principal);
    Account user = accountRepository.findAccountByUsername(tvPrincipal.getUserName());
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    return user.getEmail().equals(mapping.getOwnerUuid());
  }

  @Override
  public Boolean containsStudyWithShortname(URI trialverseDatasetUri, String shortName) {
    String query = StringUtils.replace(CONTAINS_STUDY_WITH_SHORTNAME, "$shortName", "'" + shortName + "'");
    return  doAskQuery(trialverseDatasetUri, query);
  }

  @Override
  @Cacheable(cacheNames="datasetHistory", key="#datasetUri.toString()")
  public Model getHistory(URI datasetUri) {
    URI uri = UriComponentsBuilder.fromHttpUrl(datasetUri.toString())
            .path(WebConstants.HISTORY_ENDPOINT)
            .build()
            .toUri();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

    ResponseEntity<Graph> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    return ModelFactory.createModelForGraph(responseEntity.getBody());
  }

  @Override
  public JSONObject executeHeadQuery(String sparqlQuery, VersionMapping versionMapping) {
    ResponseEntity<JSONObject> responseEntity = doRequest(versionMapping, sparqlQuery, WebConstants.APPLICATION_SPARQL_RESULTS_JSON, JSONObject.class);
    JSONObject jsonObject = responseEntity.getBody();
    String versionUri = responseEntity.getHeaders().get(WebConstants.X_EVENT_SOURCE_VERSION).get(0);
    jsonObject.put(WebConstants.VERSION_UUID, versionUri.split("/")[4]);
    return jsonObject;
  }


  private String getDatasetUri(Graph datasetGraph) {
    ExtendedIterator<Triple> datasetIterator = datasetGraph.find(Node.ANY, Node.ANY, Node.ANY);
    Triple datasetTriple = datasetIterator.next();
    return datasetTriple.getSubject().getURI();
  }

  private byte[] executeRequestAndCloseResponse(HttpGet request) throws IOException {
    HttpResponse response = httpClient.execute(request);
    try (InputStream contentStream = response.getEntity().getContent()) {
      byte[] content = IOUtils.toByteArray(contentStream);
      EntityUtils.consume(response.getEntity());
      return content;
    }
  }

  private Graph addDatasetType(String trialverseDatasetUrl, Graph sourceGraph) {
    Graph graph = GraphFactory.createGraphMem();
    GraphUtil.addInto(graph, sourceGraph);
    graph.add(new Triple(NodeFactory.createURI(trialverseDatasetUrl), RDF.Nodes.type, CLASS_VOID_DATASET));
    return graph;
  }

  private Graph addCreator(String trialverseDatasetUrl, String creatorUuid, Graph sourceGraph) {
    Graph graph = GraphFactory.createGraphMem();
    GraphUtil.addInto(graph, sourceGraph);
    graph.add(new Triple(NodeFactory.createURI(trialverseDatasetUrl), DCTerms.creator.asNode(), NodeFactory.createLiteral(creatorUuid)));
    return graph;
  }
}
