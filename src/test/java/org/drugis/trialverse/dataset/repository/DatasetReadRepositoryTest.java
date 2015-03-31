package org.drugis.trialverse.dataset.repository;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.impl.DatasetReadRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.JenaGraphMessageConverter;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import sun.security.acl.PrincipalImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DatasetReadRepositoryTest {


  @Mock
  WebConstants webConstants;

  @Mock
  JenaFactory jenaFactory;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  RestTemplate restTemplate;

  @Mock
  HttpClient httpClient;

  @InjectMocks
  DatasetReadRepository datasetReadRepository;

  @Before
  public void init() throws IOException {

    webConstants = mock(WebConstants.class);
    jenaFactory = mock(JenaFactory.class);

    datasetReadRepository = new DatasetReadRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    when(webConstants.getTriplestoreBaseUri()).thenReturn("baseUri");

  }

  @Test
  public void testQueryDatasets() throws Exception {
    Account account = new Account(1, "username", "firstName", "lastName");
    String datasetLocation = "loc1";
    String versionKey = "version1";
    VersionMapping versionMapping = new VersionMapping(1, datasetLocation, account.getUsername(), versionKey);
    String datasetLocation2 = "loc2";
    String versionKey2 = "version2";
    VersionMapping versionMapping2 = new VersionMapping(2, datasetLocation2, account.getUsername(), versionKey2);
    List<VersionMapping> mockResult = Arrays.asList(versionMapping, versionMapping2);
    when(versionMappingRepository.findMappingsByUsername(account.getUsername())).thenReturn(mockResult);
    when(webConstants.getTriplestoreBaseUri()).thenReturn("http://mockserver/");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "http://myhost/myversion");
    ResponseEntity<Graph> responseEntity = new ResponseEntity<>(GraphFactory.createGraphMem(), httpHeaders, HttpStatus.OK);
    List<HttpMessageConverter<?>> convertorList = new ArrayList<>();
    convertorList.add(new JenaGraphMessageConverter());
    when(restTemplate.getMessageConverters()).thenReturn(convertorList);
    when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(responseEntity);


    Model model = datasetReadRepository.queryDatasets(account);

    assertNotNull(model);
  }

  @Test
  public void testGetVersionedDataset() throws URISyntaxException {
    String datasetUUID = "uuid";
    String versionUuid = "versionUuid";
    String versioneduri = "versioneduri";
    VersionMapping mapping = new VersionMapping(versioneduri, "itsame", Namespaces.DATASET_NAMESPACE + datasetUUID);
    URI trialverseDatasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUrl)).thenReturn(mapping);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_ACCEPT_EVENT_SOURCE_VERSION, webConstants.getTriplestoreBaseUri() + DatasetReadRepository.VERSION_PATH + versionUuid);
    httpHeaders.add(org.apache.http.HttpHeaders.ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
    ResponseEntity<Graph> responseEntity = new ResponseEntity<Graph>(GraphFactory.createGraphMem(), HttpStatus.OK);
    String uri = versioneduri + DatasetReadRepository.DATA_ENDPOINT + DatasetReadRepository.QUERY_STRING_DEFAULT_GRAPH;
    when(restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class)).thenReturn(responseEntity);

    Model model = datasetReadRepository.getVersionedDataset(trialverseDatasetUrl, versionUuid);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(trialverseDatasetUrl);
    verify(restTemplate).exchange(uri, HttpMethod.GET, requestEntity, Graph.class);
    assertNotNull(model);
  }

  @Test
  public void testIsOwnerWhenQuerySaysFalse() throws IOException, URISyntaxException {
    URI datasetUrl = new URI("datasetUUID");
    String user1 = "other user";
    Principal principal = new PrincipalImpl("user");
    VersionMapping versionMapping = new VersionMapping(1, "whatever", user1, datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    Boolean result = datasetReadRepository.isOwner(datasetUrl, principal);

    assertFalse(result);
  }

  @Test
  public void testIsOwnerWhenQuerySaysTrue() throws IOException, URISyntaxException {
    String user1 = "user1";
    Principal principal = new PrincipalImpl(user1);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + "datasetUUID");
    VersionMapping versionMapping = new VersionMapping(1, "whatever", user1, datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    Boolean result = datasetReadRepository.isOwner(datasetUrl, principal);

    assertTrue(result);
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
    httpHeaders.add(org.apache.http.HttpHeaders.ACCEPT, acceptType);
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

    verify(restTemplate).exchange(uriComponents.toUri(), HttpMethod.GET, requestEntity, JsonObject.class);
    assertTrue(result);
  }

  @Test
  public void testGetHistory() throws URISyntaxException, IOException {
    String user1 = "user1";
    URI datasetUrl = new URI("uuid-1");
    String versionedDatasetUrl = "http://whatever";
    HttpResponse mockResponse = mock(HttpResponse.class);
    VersionMapping versionMapping = new VersionMapping(1, versionedDatasetUrl, user1, datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);
    when(httpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
    HttpResponse actualHttpResponse = datasetReadRepository.getHistory(datasetUrl);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(httpClient).execute(any(HttpGet.class));
    assertEquals(mockResponse, actualHttpResponse);
  }

  @Test
  public void testExecuteVersionedQuery() throws URISyntaxException, IOException {
    String datasetUUID = "datasetUUID";
    String query = "SELECT * WHERE { ?s ?p ?o }";
    String acceptHeader = "confirm/deny";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    VersionMapping versionMapping = new VersionMapping(1, "http://whatever", "pietje@precies.gov", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    datasetReadRepository.executeQuery(query, datasetUrl, null, acceptHeader);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(httpClient).execute(any(HttpGet.class));
  }

  @Test
  public void testExecuteHeadQuery() throws URISyntaxException, IOException {
    String datasetUUID = "datasetUUID";
    String query = "SELECT * WHERE { ?s ?p ?o }";
    String versionUuid = "myVersion";
    String acceptHeader = "confirm/deny";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    VersionMapping versionMapping = new VersionMapping(1, "http://whatever", "pietje@precies.gov", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    datasetReadRepository.executeQuery(query, datasetUrl, versionUuid, acceptHeader);

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(httpClient).execute(any(HttpGet.class));
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(jenaFactory);
  }


  /**
   * parses the response from the HttpResponse, use for debugging only !!!
   *
   * @param response
   * @return
   * @throws Exception
   */
  private static String parseResponse(HttpResponse response) throws Exception {
    String result = null;
    BufferedReader reader = null;
    try {
      Header contentEncoding = response.getFirstHeader("Content-Encoding");
      if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(response.getEntity().getContent())));
      } else {
        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      }
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      result = sb.toString();
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return result;
  }
}