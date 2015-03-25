package org.drugis.trialverse.graph.repository;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.impl.GraphReadRepositoryImpl;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  public void testGetGraph() throws IOException, URISyntaxException {
    String datasetUUID = "datasetUUID";
    String graphUUID = "uuid";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);

    VersionMapping mapping = new VersionMapping("http://versionedDatsetUrl", "ownerUuid", trialverseDatasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);

    graphReadRepository.getGraph(trialverseDatasetUri, graphUUID);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(mapping.getVersionedDatasetUrl())
            .path("/data")
            .queryParam("graph", Namespaces.GRAPH_NAMESPACE + graphUUID)
            .build();

    HttpGet request = new HttpGet(uriComponents.toUri());
    verify(httpClient).execute(any(HttpGet.class));

  }
}
