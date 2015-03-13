package org.drugis.trialverse.study.repository;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.study.repository.impl.StudyWriteRepositoryImpl;
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
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class StudyWriteRepositoryTest {

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

  @InjectMocks
  StudyWriteRepository studyWriteRepository;

  @Before
  public void setUp() throws IOException {
    webConstants = mock(WebConstants.class);
    studyWriteRepository = new StudyWriteRepositoryImpl();
    initMocks(this);
    reset(httpClientFactory, mockHttpClient);
    when(webConstants.getTriplestoreDataUri()).thenReturn("BaseUri/current");
    when(httpClientFactory.build()).thenReturn(mockHttpClient);

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(mockHttpClient);
  }

  @Test
  public void testUpdateStudy() throws IOException, URISyntaxException {
    String datasetUuid = "datasetuuid";
    String studyUuid = "studyUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionStoreDatasetUri = "versionStoreDatasetUri";
    VersionMapping versionMapping = new VersionMapping(1, versionStoreDatasetUri, "userName", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    studyWriteRepository.updateStudy(datasetUrl, studyUuid, delegatingServletInputStream);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<DelegatingServletInputStream> requestEntity = new HttpEntity<>(delegatingServletInputStream, httpHeaders);
    String uri = versionMapping.getVersionedDatasetUrl() + "/data?graph={graphUri}";
    verify(restTemplate).put(uri, requestEntity, Namespaces.STUDY_NAMESPACE + studyUuid);
  }

}