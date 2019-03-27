package org.drugis.addis.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.drugis.addis.importer.ClinicalTrialsImportService;
import org.drugis.addis.importer.impl.ClinicalTrialsImportError;
import org.drugis.addis.importer.impl.ClinicalTrialsImportServiceImpl;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClinicalTrialsImportServiceTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private HttpClient httpClient = mock(HttpClient.class);

  @Mock
  private GraphWriteRepository graphWriteRepository = mock(GraphWriteRepository.class);

  @InjectMocks
  private ClinicalTrialsImportService clinicalTrialsImportService;

  @Before
  public void setUp() {
    clinicalTrialsImportService = new ClinicalTrialsImportServiceImpl();
    initMocks(this);
  }

  @Test
  public void fetchInfo() throws ClinicalTrialsImportError, IOException {
    String ntcID = "ntcId";
    String jsonObjectString = "{\"foo\": \"bar\"}";
    JsonNode mockResultFromService = objectMapper.readTree(jsonObjectString);
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);
    InputStream inputStream = new ByteArrayInputStream(jsonObjectString.getBytes());
    when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
    when(entity.getContent()).thenReturn(inputStream);
    when(response.getEntity()).thenReturn(entity);
    when(httpClient.execute(any())).thenReturn(response);

    JsonNode result = clinicalTrialsImportService.fetchInfo(ntcID);

    assertEquals(result, mockResultFromService);
  }

  @Test
  public void fetchInfoNotFound() throws ClinicalTrialsImportError, IOException {
    String ntcID = "ntcId";

    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "FINE!"));
    when(httpClient.execute(any())).thenReturn(response);

    JsonNode result = clinicalTrialsImportService.fetchInfo(ntcID);
    assertNull(result);
  }

  @Test(expected = ClinicalTrialsImportError.class)
  public void fetchInfoOtherCode() throws ClinicalTrialsImportError, IOException {
    String ntcID = "ntcId";
    BasicHttpResponse response = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("p", 1, 1), 12345, "reason")
    );
    when(httpClient.execute(any())).thenReturn(response);

    clinicalTrialsImportService.fetchInfo(ntcID);
  }

  @Test
  public void testImportStudy() throws ClinicalTrialsImportError, IOException, UpdateGraphException {
    String commitTitle = "title";
    String commitDesc = "desc";
    String datasetUuid = "dataset";
    String graphUuid = "graph";
    String studyRef = "123435ABC";
    String jsonObjectString = "{\"foo\": \"bar\"}";
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);
    InputStream inputStream = new ByteArrayInputStream(jsonObjectString.getBytes());
    when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
    when(entity.getContent()).thenReturn(inputStream);
    when(response.getEntity()).thenReturn(entity);
    when(httpClient.execute(any())).thenReturn(response);
    Header mockHeader = mock(Header.class);
    when(graphWriteRepository.updateGraph(URI.create(datasetUuid), graphUuid, inputStream, commitTitle, commitDesc)).thenReturn(mockHeader);

    Header result = clinicalTrialsImportService.importStudy(commitTitle, commitDesc, datasetUuid, graphUuid, studyRef);
    assertEquals(mockHeader, result);
  }
}
