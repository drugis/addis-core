package org.drugis.trialverse.dataset.repository;


import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicStatusLine;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.graph.GraphFactory;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.impl.DatasetReadRepositoryImpl;
import org.drugis.trialverse.util.JenaGraphMessageConverter;
import org.drugis.trialverse.util.Namespaces;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DatasetReadRepositoryTest {

  @Mock
  private JenaFactory jenaFactory;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private HttpClient httpClient;

  @InjectMocks
  private DatasetReadRepository datasetReadRepository;

  @Before
  public void init() {
    datasetReadRepository = new DatasetReadRepositoryImpl();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(jenaFactory, accountRepository, versionMappingRepository);
  }


  @Test
  public void testQueryDatasets() {
    Account account = new Account(1, "username", "firstName", "lastName", "foo@bar.com");
    String datasetLocation = "loc1";
    String versionKey = "version1";
    VersionMapping versionMapping = new VersionMapping(1, datasetLocation, account.getEmail(), versionKey);
    String datasetLocation2 = "loc2";
    String versionKey2 = "version2";
    VersionMapping versionMapping2 = new VersionMapping(2, datasetLocation2, account.getEmail(), versionKey2);
    List<VersionMapping> mockResult = Arrays.asList(versionMapping, versionMapping2);
    when(versionMappingRepository.findMappingsByEmail(account.getEmail())).thenReturn(mockResult);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "http://myhost/myversion");
    Graph datasetGraph = GraphFactory.createGraphMem();
    datasetGraph.add(new Triple(NodeFactory.createURI("http://anything.com"),
            NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            NodeFactory.createURI("http://trials.drugis.org/ontology#Dataset")));
    ResponseEntity<Object> responseEntity = new ResponseEntity<>(datasetGraph, httpHeaders, HttpStatus.OK);
    List<HttpMessageConverter<?>> convertorList = new ArrayList<>();
    convertorList.add(new JenaGraphMessageConverter());
    when(restTemplate.getMessageConverters()).thenReturn(convertorList);
    when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(responseEntity);

    Model model = datasetReadRepository.queryDatasets(account);

    assertNotNull(model);

    verify(versionMappingRepository).findMappingsByEmail(account.getEmail());
  }

  @Test
  public void testGetVersionedDataset() throws URISyntaxException {
    String datasetUuid = "uuid";
    String versionUuid = "versionUuid";
    String versionedUri = "baseUri/versions/" + versionUuid;
    VersionMapping mapping = new VersionMapping(versionedUri, "itsame", Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI trialverseDatasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUrl)).thenReturn(mapping);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_ACCEPT_EVENT_SOURCE_VERSION, WebConstants.getVersionBaseUri() + versionUuid);
    httpHeaders.add(ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    Graph datasetGraph = GraphFactory.createGraphMem();
    datasetGraph.add(new Triple(NodeFactory.createURI(trialverseDatasetUrl.toString()),
            NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            NodeFactory.createURI("http://trials.drugis.org/ontology#Dataset")));
    ResponseEntity<Graph> responseEntity = new ResponseEntity<>(datasetGraph, HttpStatus.OK);
    String uri = versionedUri + WebConstants.DATA_ENDPOINT + WebConstants.QUERY_STRING_DEFAULT_GRAPH;
    when(restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class)).thenReturn(responseEntity);

    Model model = datasetReadRepository.getVersionedDataset(trialverseDatasetUrl, versionUuid);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(trialverseDatasetUrl);
    verify(restTemplate).exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    assertNotNull(model);
    StringWriter writer = new StringWriter();
    model.write(writer);
    String expectedGraph = "<rdf:RDF\n" +
            "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
            "    xmlns:j.0=\"http://purl.org/dc/terms/\"\n" +
            "    xmlns:j.1=\"http://trials.drugis.org/ontology#\"\n" +
            "    xmlns:j.2=\"http://rdfs.org/ns/void#\">\n" +
            "  <j.1:Dataset rdf:about=\"http://trials.drugis.org/datasets/uuid\">\n" +
            "    <j.0:creator>itsame</j.0:creator>\n" +
            "    <rdf:type rdf:resource=\"http://rdfs.org/ns/void#Dataset\"/>\n" +
            "  </j.1:Dataset>\n" +
            "</rdf:RDF>\n";
    assertThat(writer.toString(), equalToIgnoringWhiteSpace(expectedGraph));
  }

  @Test
  public void testIsOwnerWhenQuerySaysFalse() throws URISyntaxException {
    URI datasetUrl = new URI("datasetUuid");
    String user1 = "other user";
    Account account = new Account(user1, "piet", "klaassen", "foo@bar.com");
    ApiKey credentials = new ApiKey();
    Principal principal = new PreAuthenticatedAuthenticationToken(account, credentials);
    VersionMapping versionMapping = new VersionMapping(1, "whatever", "different@email.com", datasetUrl.toString());
    when(accountRepository.findAccountByUsername(account.getUsername())).thenReturn(account);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    Boolean result = datasetReadRepository.isOwner(datasetUrl, principal);

    assertFalse(result);

    verify(accountRepository).findAccountByUsername(user1);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
  }

  @Test
  public void testIsOwnerWhenQuerySaysTrue() throws URISyntaxException {
    String user1 = "user1";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + "datasetUuid");
    Account account = new Account(user1, "piet", "klaassen", "foo@bar.com");
    ApiKey credentials = new ApiKey();
    Principal principal = new PreAuthenticatedAuthenticationToken(account, credentials);
    VersionMapping versionMapping = new VersionMapping(1, "whatever", account.getEmail(), datasetUrl.toString());
    when(accountRepository.findAccountByUsername(account.getUsername())).thenReturn(account);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    Boolean result = datasetReadRepository.isOwner(datasetUrl, principal);

    assertTrue(result);

    verify(accountRepository).findAccountByUsername(user1);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
  }

  @Test
  public void testContainsStudyWithShortName() throws IOException, URISyntaxException {
    String user1 = "user1";
    URI datasetUrl = new URI("uuid-1");
    String shortName = "shortName";
    String versionedDatasetUrl = "http://whatever";
    String acceptType = RDFLanguages.JSONLD.getContentType().getContentType();
    VersionMapping versionMapping = new VersionMapping(1, versionedDatasetUrl, user1, datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, WebContent.contentTypeSPARQLQuery);
    httpHeaders.add(ACCEPT, acceptType);
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    ResponseEntity responseEntity = new ResponseEntity<>(JSON.parse("{\"boolean\":true}"), HttpStatus.OK);
    String containsStudyWithShortNameTemplate = IOUtils.toString(new ClassPathResource("askContainsStudyWithLabel.sparql").getInputStream(), "UTF-8");
    String query = containsStudyWithShortNameTemplate.replace("$shortName", "'shortName'");
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path("/query")
            .queryParam("query", query)
            .build();
    when(restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, requestEntity, JsonObject.class)).thenReturn(responseEntity);

    Boolean result = datasetReadRepository.containsStudyWithShortname(datasetUrl, shortName);

    assertTrue(result);

    verify(restTemplate).exchange(uriComponents.toUri(), HttpMethod.GET, requestEntity, JsonObject.class);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);

  }

  @Test
  public void testGetHistory() throws URISyntaxException, IOException {
    String versionedDatasetUrl = "http://whatever";
    URI uri = new URI(versionedDatasetUrl + WebConstants.HISTORY_ENDPOINT);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    ResponseEntity<Graph> responseEntity = new ResponseEntity<>(GraphFactory.createGraphMem(), HttpStatus.OK);
    when(restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class)).thenReturn(responseEntity);

    Model historyModel = datasetReadRepository.getHistory(URI.create(versionedDatasetUrl));

    verify(restTemplate).exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    assertNotNull(historyModel);
    assertEquals(ModelFactory.createModelForGraph(responseEntity.getBody()), historyModel);
  }

  @Test
  public void testExecuteQueryOnHead() throws URISyntaxException, IOException {
    String datasetUuid = "datasetUuid";
    String query = "SELECT * WHERE { ?s ?p ?o }";
    String acceptHeader = "confirm/deny";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    VersionMapping versionMapping = new VersionMapping(1, "http://whatever", "pietje@precies.gov", datasetUrl.toString());
    HttpResponse mockResponse = mock(CloseableHttpResponse.class);
    org.apache.http.HttpEntity entity = mock(org.apache.http.HttpEntity.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, org.apache.http.HttpStatus.SC_OK, "FINE!"));
    String responceString = "check me out";
    when(entity.getContent()).thenReturn(IOUtils.toInputStream(responceString, defaultCharset()));
    when(mockResponse.getEntity()).thenReturn(entity);
    when(httpClient.execute(any(HttpPut.class))).thenReturn(mockResponse);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    datasetReadRepository.executeQuery(query, datasetUrl, null, acceptHeader);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(httpClient).execute(any(HttpGet.class));
  }

  @Test
  public void testExecuteVersionedQuery() throws URISyntaxException, IOException {
    String datasetUuid = "datasetUuid";
    String query = "SELECT * WHERE { ?s ?p ?o }";
    String versionUuid = "http://myVersion.ninja";
    String acceptHeader = "confirm/deny";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    VersionMapping versionMapping = new VersionMapping(1, "http://whatever", "pietje@precies.gov", datasetUrl.toString());

    HttpResponse mockResponse = mock(CloseableHttpResponse.class);
    org.apache.http.HttpEntity entity = mock(org.apache.http.HttpEntity.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, org.apache.http.HttpStatus.SC_OK, "FINE!"));
    String responceString = "check me out";
    when(entity.getContent()).thenReturn(IOUtils.toInputStream(responceString, defaultCharset()));
    when(mockResponse.getEntity()).thenReturn(entity);
    when(httpClient.execute(any(HttpPut.class))).thenReturn(mockResponse);
    when(httpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    datasetReadRepository.executeQuery(query, datasetUrl, URI.create(versionUuid), acceptHeader);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(httpClient).execute(any(HttpGet.class));
  }

  @Test
  public void testTextQuery() throws Exception {

    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("despressionStudyExample1.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    dataset.addNamedModel("http://study1", model1);

    Model model2 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph2 = new ClassPathResource("despressionStudyExample2.ttl").getInputStream();
    model1.read(mockStudyGraph2, null, "TTL");
    dataset.addNamedModel("http://study2", model2);

    String template = IOUtils.toString(new ClassPathResource("findStudiesByTerms.sparql")
            .getInputStream(), "UTF-8");
    CharSequence searchTerm = "Depress";
    String queryStr = template.replace("$searchTerm", searchTerm);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();

    resultSet.forEachRemaining(results::add);

    assertEquals(2, results.size());
  }

  @Test
  public void testExecuteHeadQuery() throws URISyntaxException, ParseException {
    VersionMapping versionMapping = new VersionMapping("http://versionUrl", "owner", "trialverseUrl");
    String sparqlQuery = "sparqlQuery";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, WebContent.contentTypeSPARQLQuery);
    String acceptType = WebConstants.APPLICATION_SPARQL_RESULTS_JSON;
    httpHeaders.add(ACCEPT, acceptType);
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "http://localhost:8080/versions/versionUuid");

    ResponseEntity responseEntity = new ResponseEntity<>(new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse("{\"study\":\"bla\"}"), responseHeaders, HttpStatus.OK);
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path("/query")
            .queryParam("query", sparqlQuery)
            .build();
    when(restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, requestEntity, JSONObject.class)).thenReturn(responseEntity);


    JSONObject object = datasetReadRepository.executeHeadQuery(sparqlQuery, versionMapping);
    assertNotNull(object);
  }
}