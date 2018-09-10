package org.drugis.trialverse.graph.repository;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicStatusLine;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.exception.ReadGraphException;
import org.drugis.trialverse.graph.repository.impl.GraphReadRepositoryImpl;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.addis.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 28-11-14.
 */
public class GraphReadRepositoryTest {
  @Mock
  HttpClient httpClient;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  GraphService graphService;

  @InjectMocks
  GraphReadRepository graphReadRepository;

  @Before
  public void init() {
    graphReadRepository = new GraphReadRepositoryImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetGraph() throws IOException, ReadGraphException {
    String graphUUID = "graphUuid";
    String versionUuid = "versionUuid";
    String versionedDatasetUrl = "http://myversiondDatasetUrl";
    HttpResponse mockResponse = mock(CloseableHttpResponse.class);
    org.apache.http.HttpEntity entity = mock(org.apache.http.HttpEntity.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, org.apache.http.HttpStatus.SC_OK, "FINE!"));
    String responseString = "check me out";
    when(entity.getContent()).thenReturn(IOUtils.toInputStream(responseString, defaultCharset()));
    when(mockResponse.getEntity()).thenReturn(entity);
    when(graphService.buildGraphUri(graphUUID)).thenReturn(URI.create(Namespaces.GRAPH_NAMESPACE + graphUUID));
    when(httpClient.execute(any(HttpPut.class))).thenReturn(mockResponse);
    graphReadRepository.getGraph(versionedDatasetUrl, versionUuid, graphUUID, WebConstants.TURTLE);

   UriComponentsBuilder.fromHttpUrl(versionedDatasetUrl)
            .path("/data")
            .queryParam("graph", Namespaces.GRAPH_NAMESPACE + graphUUID)
            .build();

    verify(httpClient).execute(any(HttpGet.class));

  }
}
