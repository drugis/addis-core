package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.drugis.addis.trialverse.service.impl.ClinicalTrialsImportError;
import org.drugis.addis.trialverse.service.impl.ClinicalTrialsImportServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 12-5-16.
 */
public class ClinicalTrialsImportServiceTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock HttpClient httpClient = mock(HttpClient.class);

  @InjectMocks ClinicalTrialsImportService clinicalTrialsImportService;

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
    BasicHttpResponse response = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("p", 1, 1), HttpStatus.SC_OK, "reason")
    );
    BasicHttpEntity entity = new BasicHttpEntity();
    InputStream inputStream = new ByteArrayInputStream(jsonObjectString.getBytes());
    entity.setContent(inputStream);
    response.setEntity(entity);
    when(httpClient.execute(any())).thenReturn(response);

    JsonNode result = clinicalTrialsImportService.fetchInfo(ntcID);

    assertEquals(result, mockResultFromService);
  }

  @Test
  public void fetchInfoNotFound() throws ClinicalTrialsImportError, IOException {
    String ntcID = "ntcId";
    BasicHttpResponse response = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("p", 1, 1), HttpStatus.SC_NOT_FOUND, "reason")
    );
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
}
