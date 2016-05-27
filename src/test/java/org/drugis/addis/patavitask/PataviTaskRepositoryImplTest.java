package org.drugis.addis.patavitask;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.util.WebConstants;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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


}
