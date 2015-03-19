package org.drugis.trialverse.study.repository;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.study.repository.impl.GraphReadRepositoryImpl;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 28-11-14.
 */
public class GraphReadRepositoryTest {

  @Mock
  WebConstants webConstants;

  @Mock
  HttpClient httpClient;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @InjectMocks
  GraphReadRepository graphReadRepository;

  @Before
  public void init() throws IOException {
    graphReadRepository = new GraphReadRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    when(webConstants.getTriplestoreBaseUri()).thenReturn("baseUri");
  }

  @Test
  public void testGetStudy() throws IOException, URISyntaxException {
    String datasetUUID = "datasetUUID";
    String studyUUID = "uuid";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);

    VersionMapping mapping = new VersionMapping("http://versionedDatsetUrl", "ownerUuid", trialverseDatasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);

    graphReadRepository.getStudy(trialverseDatasetUri, studyUUID);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(mapping.getVersionedDatasetUrl())
            .path("/data")
            .queryParam("graph", Namespaces.STUDY_NAMESPACE + studyUUID)
            .build();

    HttpGet request = new HttpGet(uriComponents.toUri());
    verify(httpClient).execute(any(HttpGet.class));

  }
}
