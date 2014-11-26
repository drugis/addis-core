package org.drugis.trialverse.study.repository;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.study.repository.impl.StudyWriteRepositoryImpl;
import org.drugis.trialverse.testutils.TestUtils;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

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
    when(mockHttpClient.execute(any(HttpEntityEnclosingRequestBase.class))).thenReturn(mockResponse);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(mockHttpClient);
  }

  @Test
  public void testCreateStudy() throws IOException {
    String studyJson = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    HttpResponse response = studyWriteRepository.createStudy("test", studyJson);
    assertNotNull(response);
    verify(mockHttpClient).execute(any(HttpPut.class));
  }
  @Test
  public void testUpdateStudy() throws IOException {
    String studyJson = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    studyWriteRepository.updateStudy("test", studyJson);
    verify(mockHttpClient).execute(any(HttpPost.class));  // FIXME: something weird here, also succeeds on HttpPut.class
  }

}