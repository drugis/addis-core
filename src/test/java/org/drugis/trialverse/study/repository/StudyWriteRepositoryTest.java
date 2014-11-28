package org.drugis.trialverse.study.repository;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.study.repository.impl.StudyWriteRepositoryImpl;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.DelegatingServletInputStream;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
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
  public void testCreateStudy() throws IOException {
    String studyUUID = "studyUUID";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
    HttpResponse response = studyWriteRepository.createStudy(studyUUID, "");

    assertNotNull(response);
    verify(mockHttpClient).execute(any(HttpPut.class)); // todo fix this
  }

  @Test
  public void testUpdateStudy() throws IOException {
    String studyUUID = "studyUUID";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);

    HttpResponse response = studyWriteRepository.updateStudy(studyUUID, "");

    verify(mockHttpClient).execute(any(HttpPut.class));
    // FIXME: something weird here, also succeeds on HttpPut.class
  }

}