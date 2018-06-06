package org.drugis.addis.patavitask;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.util.WebConstants;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(JUnit4.class)
public class PataviTaskRepositoryImplTest {

  @Mock
  WebConstants webConstants;

  @Mock
  HttpClient httpClient;

  @InjectMocks
  private PataviTaskRepository pataviTaskRepository;

  @Before
  public void setUp() {
    pataviTaskRepository = new PataviTaskRepositoryImpl();
    initMocks(this);
    when(webConstants.getPataviUri()).thenReturn("https://localhost:3000");
  }

  @Test
  public void testCreateTask() throws Exception {
    String createdLocation = "testLocation";

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    Header locationHeader = new BasicHeader("Location", "testLocation");
    when(mockResponse.getHeaders("Location")).thenReturn(new Header[]{locationHeader});
    when(httpClient.execute(any())).thenReturn(mockResponse);
    JSONObject problem = new JSONObject();

    URI createdUri = pataviTaskRepository.createPataviTask(URI.create("foo"), problem);

    assertEquals(createdLocation, createdUri.toString());
  }

  @Test
  public void testGetResult() throws Exception {
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    HttpEntity entity = new StringEntity("{\"key\": \"value\"}");

    when(mockResponse.getEntity()).thenReturn(entity);
    when(httpClient.execute(any())).thenReturn(mockResponse);

    URI task = URI.create("https://example.com");
    JsonNode results = pataviTaskRepository.getResult(task);

    assertEquals("value", results.get("key").toString());
  }

}
