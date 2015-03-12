package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.jayway.jsonpath.JsonPath;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

/**
 * Created by daan on 7-11-14.
 */
@Repository
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  private static final Logger logger = LoggerFactory.getLogger(DatasetReadRepositoryImpl.class);
  private static final String STUDIES_WITH_DETAILS = loadResource("constructStudiesWithDetails.sparql");
  private static final String CONTAINS_STUDY_WITH_SHORTNAME = loadResource("askContainsStudyWithLabel.sparql");
  private static final String QUERY_AFFIX = "/current/query";
  private static final String DATA_ENDPOINT = "/data";
  private static final String QUERY_STRING_DEFAULT_GRAPH = "?default";

  private static final Node CLASS_VOID_DATASET = NodeFactory.createURI("http://rdfs.org/ns/void#Dataset");
  public static final String QUERY_ENDPOINT = "/query";


  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private WebConstants webConstants;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private RestTemplate restTemplate;

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
    throw new LoadResourceException("could not load resource " + filename);
  }

  private HttpResponse doSelectQuery(String query) {
    return doRequest(query, FUSEKI_OUTPUT_TYPES.JSON.toString(), RDFLanguages.JSONLD.getContentType().getContentType());
  }

  private HttpResponse doAskQuery(String query) {
    return doRequest(query, FUSEKI_OUTPUT_TYPES.JSON.toString(), RDFLanguages.JSONLD.getContentType().getContentType());
  }


  private HttpResponse doRequest(URI trialverseDatasetUri, String query, String outputType, String acceptType) {
//    try {
//      HttpClient client = httpClientFactory.build();
//      URIBuilder builder = new URIBuilder(webConstants.getTriplestoreBaseUri() + QUERY_AFFIX);
//      builder.setParameter("query", query);
//      builder.setParameter("output", outputType);
//      HttpGet request = new HttpGet(builder.build());
//      request.setHeader("Accept", acceptType);
//      return client.execute(request);
//    } catch (URISyntaxException | IOException e) {
//      logger.error(e.toString());
//    }
//    throw new QueryException("Could not execute query " + query);
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, );
    WebContent.contentTypeSPARQLUpdate
    HttpEntity<String> requestEntity = new HttpEntity<>(query, httpHeaders);
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl()).path(QUERY_ENDPOINT).build();

    restTemplate.getForEntity(uriComponents.toUri(), HttpResponse.class);
  }

  private Graph addDatasetType(String trialverseDatasetUrl, Graph datasetGraph) {
    Graph graph = GraphFactory.createGraphMem();
    GraphUtil.addInto(graph, datasetGraph);
    graph.add(new Triple(NodeFactory.createURI(trialverseDatasetUrl), RDF.Nodes.type, CLASS_VOID_DATASET));
    return graph;
  }

  @Override
  public Model queryDatasets(Account currentUserAccount) {
    List<VersionMapping> mappings = versionMappingRepository.findMappingsByUsername(currentUserAccount.getUsername());
    Graph graph = GraphFactory.createGraphMem();

    for (VersionMapping mapping : mappings) {

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add(org.apache.http.HttpHeaders.ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
      HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
      String uri = mapping.getVersionedDatasetUrl() + DATA_ENDPOINT + QUERY_STRING_DEFAULT_GRAPH;

      ResponseEntity<Graph> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class);

      GraphUtil.addInto(graph, responseEntity.getBody());
      graph = addDatasetType(mapping.getTrialverseDatasetUrl(), graph);
    }

    return ModelFactory.createModelForGraph(graph);
  }

  @Override
  public Model getDataset(String datasetUUID) {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(Namespaces.DATASET_NAMESPACE + datasetUUID);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    String uri = versionMapping.getVersionedDatasetUrl() + DATA_ENDPOINT + QUERY_STRING_DEFAULT_GRAPH;

    ResponseEntity<Graph> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    Graph graph = addDatasetType(versionMapping.getTrialverseDatasetUrl(), responseEntity.getBody());
    return ModelFactory.createModelForGraph(graph);
  }

  @Override
  public HttpResponse queryDatasetsWithDetail(String datasetUUID) {
    String query = StringUtils.replace(STUDIES_WITH_DETAILS, "$datasetUUID", datasetUUID);
    return doSelectQuery(query);
  }

  @Override
  public boolean isOwner(String datasetUUID, Principal principal) {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(Namespaces.DATASET_NAMESPACE + datasetUUID);
    return principal.getName().equals(mapping.getOwnerUuid());
  }

  @Override
  public boolean containsStudyWithShortname(String datasetUUID, String shortName) {
    Boolean containsStudyWithShortname = false;
    String query = StringUtils.replace(CONTAINS_STUDY_WITH_SHORTNAME, "$dataset", datasetUUID);
    query = StringUtils.replace(query, "$shortName", "'" + shortName + "'");
    HttpResponse response = doAskQuery(query);
    try {
      containsStudyWithShortname = JsonPath.read(response.getEntity().getContent(), "$.boolean");
    } catch (IOException e) {
      logger.error(e.toString());
    }

    return containsStudyWithShortname;
  }

  private static class LoadResourceException extends RuntimeException {
    public LoadResourceException(String s) {
      super(s);
    }
  }
}
