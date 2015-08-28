package org.drugis.trialverse.graph.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.service.impl.GraphServiceImpl;
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

  @Before
  public void setUp() {
    graphService = new GraphServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCopy() throws Exception {
    URI targetDatasetUri = new URI("http://target.dataset");
    URI targetGraphUri = new URI("http://target.graph.uri");
    URI sourceDatasetUri = new URI("http://source.dataset.uri");
    URI sourceGraphUri = new URI("http://trials.drugis.org/graphs/0ef5c6e8-a5fb-4ac0-953b-d1a75fdf9312");
    URI sourceVersionUri = new URI("http://localhost:8080/versions/e53caa0d-c0df-46db-977e-37f48fecb042");
    URI revisionUri = new URI("http://localhost:8080/revisions/e37ee2a8-14c7-4b22-87c6-fe43ac0273fe");

    Model historyModel = ModelFactory.createDefaultModel();
    InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
    historyModel.read(historyStream, null, "TTL");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.OK);
    String targetDatasetVersionedUri = "http://versionedURI";
    VersionMapping versionMapping = new VersionMapping(targetDatasetVersionedUri, null, targetDatasetUri.toString());

    when(versionMappingRepository.getVersionMappingByDatasetUrl(targetDatasetUri)).thenReturn(versionMapping);
    when(datasetReadRepository.getHistory(sourceDatasetUri)).thenReturn(historyModel);
    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetVersionedUri)
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();
    when(restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(null), String.class)).thenReturn(responseEntity);

    URI newVersion = graphService.copy(targetDatasetUri, targetGraphUri, sourceDatasetUri, sourceVersionUri, sourceGraphUri);

    assertEquals("newVersion", newVersion.toString());
  }

  @Test
  public void testExtractDatasetUuid() {
    String uri = "https://www.trialverse123.org/datasets/333-av-3222/versions/434334/graphs/44334";
    String datasetUuid = graphService.extractDatasetUuid(uri);
    assertEquals("333-av-3222", datasetUuid);
  }
  @Test
  public void testExtractVersionUuid() {
    String uri = "https://www.trialverse123.org/datasets/333-av-3222/versions/434334/graphs/44334";
    String versionUuid = graphService.extractVersionUuid(uri);
    assertEquals("434334", versionUuid);
  }
  @Test
  public void testExtractGraphUuid() {
    String uri = "https://www.trialverse123.org/datasets/333-av-3222/versions/434334/graphs/44334";
    String graphUuid = graphService.extractGraphUuid(uri);
    assertEquals("44334", graphUuid);
  }

}