package org.drugis.trialverse.graph.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.service.impl.GraphServiceImpl;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by daan on 26-8-15.
 */
public class GraphServiceTest {

  @InjectMocks
  GraphService graphService;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  DatasetReadRepository datasetReadRepository;

  @Mock
  RestTemplate restTemplate;

  @Mock
  WebConstants webConstants;

  String testHost = "http://testhost";

  @Before
  public void setUp() {
    graphService = new GraphServiceImpl();
    MockitoAnnotations.initMocks(this);
    when(webConstants.getTriplestoreBaseUri()).thenReturn(testHost);
  }

  @Test
  public void testCopy() throws Exception {
    URI targetDatasetUri = new URI("http://target.dataset");
    URI targetGraphUri = new URI("http://target.graph.uri");
    String sourceDatasetUuid = "sourceDatasetUuid";
    URI sourceGraphUri = new URI(testHost + "/datasets/" + sourceDatasetUuid + "/versions/e53caa0d-c0df-46db-977e-37f48fecb042/graphs/0ef5c6e8-a5fb-4ac0-953b-d1a75fdf9312");
    URI sourceDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + sourceDatasetUuid);
    URI revisionUri = new URI("http://testhost/revisions/e37ee2a8-14c7-4b22-87c6-fe43ac0273fe");

    Model historyModel = ModelFactory.createDefaultModel();
    InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
    historyModel.read(historyStream, null, "TTL");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.OK);
    String targetDatasetVersionedUri = "http://versionedURI";
    VersionMapping versionMapping = new VersionMapping(targetDatasetVersionedUri, null, targetDatasetUri.toString());
    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetVersionedUri)
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();

    when(versionMappingRepository.getVersionMappingByDatasetUrl(targetDatasetUri)).thenReturn(versionMapping);
    when(datasetReadRepository.getHistory(sourceDatasetUri)).thenReturn(historyModel);
    when(restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(null), String.class)).thenReturn(responseEntity);

    URI newVersion = graphService.copy(targetDatasetUri, targetGraphUri, sourceGraphUri);

    assertEquals("newVersion", newVersion.toString());
  }

  @Test
  public void testExtractDatasetUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/44334");
    URI datasetUri = graphService.extractDatasetUri(uri);
    assertEquals(Namespaces.DATASET_NAMESPACE + "333-av-3222", datasetUri.toString());
  }

  @Test
  public void testExtractVersionUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/44334");
    URI versionUri = graphService.extractVersionUri(uri);
    assertEquals("http://testhost/versions/" + "434-334", versionUri.toString());
  }

  @Test
  public void testExtractGraphUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/443-34");
    URI graphUri = graphService.extractGraphUri(uri);
    assertEquals(Namespaces.GRAPH_NAMESPACE + "443-34", graphUri.toString());
  }

}