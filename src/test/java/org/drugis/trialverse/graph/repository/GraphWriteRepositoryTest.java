package org.drugis.trialverse.graph.repository;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.jena.riot.RDFLanguages;
import org.apache.xerces.impl.dv.util.Base64;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.impl.GraphWriteRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.AuthenticationService;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GraphWriteRepositoryTest {

  HttpClient mockHttpClient = mock(HttpClient.class);
  HttpResponse mockResponse = mock(HttpResponse.class);

  @Mock
  private HttpClientFactory httpClientFactory;

  @Mock
  private WebConstants webConstants;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AuthenticationService authenticationService;

  @InjectMocks
  GraphWriteRepository graphWriteRepository;

  private String name = "foo@bar.com";

  @Before
  public void setUp() throws IOException {
    webConstants = mock(WebConstants.class);
    graphWriteRepository = new GraphWriteRepositoryImpl();
    initMocks(this);
    reset(httpClientFactory, mockHttpClient);
    when(webConstants.getTriplestoreDataUri()).thenReturn("BaseUri/current");
    when(httpClientFactory.build()).thenReturn(mockHttpClient);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(name);
    when(authenticationService.getAuthentication()).thenReturn(authentication);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(mockHttpClient);
  }

  @Test
  public void testUpdateGraphWithoutDescription() throws IOException, URISyntaxException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(webConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionStoreDatasetUri = "versionStoreDatasetUri";
    VersionMapping versionMapping = new VersionMapping(1, versionStoreDatasetUri, "userName", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    graphWriteRepository.updateGraph(datasetUrl, graphUuid, mockHttpServletRequest);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    httpHeaders.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encode(titleValue.getBytes()));
    httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + name);
    HttpEntity<DelegatingServletInputStream> requestEntity = new HttpEntity<>(delegatingServletInputStream, httpHeaders);
    String uri = versionMapping.getVersionedDatasetUrl() + "/data?graph={graphUri}";
    verify(restTemplate).put(uri, requestEntity, Namespaces.GRAPH_NAMESPACE + graphUuid);
  }

  @Test
  public void testUpdateGraphWithDescription() throws IOException, URISyntaxException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(webConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    String descriptionValue = "test description";
    when(mockHttpServletRequest.getParameter(webConstants.COMMIT_DESCRIPTION_PARAM)).thenReturn(descriptionValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionStoreDatasetUri = "versionStoreDatasetUri";
    VersionMapping versionMapping = new VersionMapping(1, versionStoreDatasetUri, "userName", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    graphWriteRepository.updateGraph(datasetUrl, graphUuid, mockHttpServletRequest);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    httpHeaders.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encode(titleValue.getBytes()));
    httpHeaders.add(WebConstants.EVENT_SOURCE_DESCRIPTION_HEADER, Base64.encode(descriptionValue.getBytes()));
    httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + name);
    HttpEntity<DelegatingServletInputStream> requestEntity = new HttpEntity<>(delegatingServletInputStream, httpHeaders);
    String uri = versionMapping.getVersionedDatasetUrl() + "/data?graph={graphUri}";
    verify(restTemplate).put(uri, requestEntity, Namespaces.GRAPH_NAMESPACE + graphUuid);
  }

}