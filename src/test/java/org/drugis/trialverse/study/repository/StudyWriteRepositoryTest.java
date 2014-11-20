package org.drugis.trialverse.study.repository;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.study.repository.impl.StudyWriteRepositoryImpl;
import org.drugis.trialverse.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class StudyWriteRepositoryTest {

  HttpClient mockHttpClient = mock(HttpClient.class);
  HttpResponse mockResponse = mock(HttpResponse.class);

  @Mock
  private HttpClientFactory httpClientFactory;

  @InjectMocks
  StudyWriteRepository studyWriteRepository;

  @Before
  public void setUp() throws IOException {
    studyWriteRepository = new StudyWriteRepositoryImpl();
    initMocks(this);
    reset(httpClientFactory, mockHttpClient);

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
    studyWriteRepository.createStudy("test", studyJson);
    verify(mockHttpClient).execute(any(HttpPut.class));
  }
  @Test
  public void testUpdateStudy() throws IOException {
    String studyJson = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    studyWriteRepository.updateStudy("test", studyJson);
    verify(mockHttpClient).execute(any(HttpPost.class));  // FIXME: something weird here, also succeeds on HttpPut.class
  }

}