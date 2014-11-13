package org.drugis.trialverse.util.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.util.service.impl.TrialverseIOUtilsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 12-11-14.
 */
public class TrialverseIOUtilsServiceTest {

  TrialverseIOUtilsService trialverseIOUtilsService;

  @Before
  public void init() {
    trialverseIOUtilsService = new TrialverseIOUtilsServiceImpl();
  }

  @Test
  public void testWriteResponseContentToServletResponse() throws IOException {
    String testContentAsString = "This is just a test !";
    HttpResponse input = mock(HttpResponse.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    InputStream inStream = new ByteArrayInputStream(testContentAsString.getBytes(StandardCharsets.UTF_8));
    when(input.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(inStream);
    when(input.getEntity().getContent()).thenReturn(inStream);
    MockHttpServletResponse output = new MockHttpServletResponse();

    trialverseIOUtilsService.writeResponseContentToServletResponse(input, output);
    assertEquals(testContentAsString, output.getContentAsString());
  }
}
