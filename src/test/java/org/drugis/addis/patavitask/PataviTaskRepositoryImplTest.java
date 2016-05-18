package org.drugis.addis.patavitask;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
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
    HttpResponse creationResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 201, "reason"));
    String createdLocation = "testLocation";
    creationResponse.addHeader("Location", createdLocation);

    when(httpClient.execute(any())).thenReturn(creationResponse);
    JSONObject problem = new JSONObject();

    URI createdUri = pataviTaskRepository.createPataviTask(problem);

    assertEquals(createdLocation, createdUri.toString());
  }


}
