package org.drugis.trialverse.study.repository;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.study.repository.impl.StudyReadRepositoryImpl;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 28-11-14.
 */
public class StudyReadRepositoryTest {

  @Mock
  HttpClientFactory httpClientFactory;

  @Mock
  WebConstants webConstants;

  @Mock
  JenaFactory jenaFactory;

  @InjectMocks
  StudyReadRepository studyReadRepository;

  HttpClient mockHttpClient;
  HttpResponse mockResponse;

  @Before
  public void init() throws IOException {
    mockHttpClient = mock(HttpClient.class);
    mockResponse = mock(HttpResponse.class, RETURNS_DEEP_STUBS);

    webConstants = mock(WebConstants.class);
    jenaFactory = mock(JenaFactory.class);

    studyReadRepository = new StudyReadRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    when(webConstants.getTriplestoreBaseUri()).thenReturn("baseUri");
    when(mockHttpClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
    when(httpClientFactory.build()).thenReturn(mockHttpClient);
  }

  @Test
  public void testGetStudy() {
    String studyUUID = "uuid";
    DatasetAccessor accessor = mock(DatasetAccessor.class);
    Model mockModel = mock(Model.class);
    when(accessor.getModel(Namespaces.STUDY_NAMESPACE + studyUUID)).thenReturn(mockModel);
    when(jenaFactory.getDatasetAccessor()).thenReturn(accessor);

    Model model = studyReadRepository.getStudy(studyUUID);

    assertEquals(mockModel, model);
    verify(jenaFactory).getDatasetAccessor();
  }
}
