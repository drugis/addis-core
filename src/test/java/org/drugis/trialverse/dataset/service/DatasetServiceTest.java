package org.drugis.trialverse.dataset.service;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.impl.DatasetServiceImpl;
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
public class DatasetServiceTest {

  @InjectMocks
  DatasetService datasetService;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  DatasetReadRepository datasetReadRepository;

  @Mock
  RestTemplate restTemplate;

  @Before
  public void setUp() {
    datasetService = new DatasetServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCopy() throws Exception {
    URI targetDatasetUri = new URI("http://target.dataset");
    URI targetGraphUri = new URI("http://target.graph.uri");
    URI sourceDatasetUri = new URI("http://source.dataset.uri");
    URI sourceGraphUri = new URI("http://trials.drugis.org/graphs/246b53de-2884-42be-ae18-e431976c1987");
    URI sourceVersionUri = new URI("http://localhost:8080/versions/90e7a2fd-d688-48d3-bcd4-006e8a8312af");
    URI revisionUri = new URI("http://localhost:8080/revisions/f589745e-47c4-4fcd-aadd-ce88551080d7");

    String versionedDatasetUri = "tmp";
    String ownerUuid = "ownerUuid";

    VersionMapping versionMapping = new VersionMapping(versionedDatasetUri, ownerUuid, sourceDatasetUri.toString());
    Model historyModel = ModelFactory.createDefaultModel();
    InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
    historyModel.read(historyStream, null, "TTL");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.OK);

    when(versionMappingRepository.getVersionMappingByDatasetUrl(sourceDatasetUri)).thenReturn(versionMapping);
    when(datasetReadRepository.getHistory(versionMapping.getVersionedDatasetUri())).thenReturn(historyModel);
    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetUri.toString())
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();
    when(restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(null), String.class)).thenReturn(responseEntity);

    URI newVersion = datasetService.copy(targetDatasetUri, targetGraphUri, sourceDatasetUri, sourceVersionUri, sourceGraphUri);

    assertEquals("newVersion", newVersion.toString());
  }
}