package org.drugis.trialverse.util.service;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.drugis.trialverse.util.service.impl.TrialverseIOUtilsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("mock protocol", 1, 0), HttpStatus.OK.value(), "some good reason");
    HttpResponse input = new BasicHttpResponse(statusLine);
    BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
    String testContentAsString = "This is just a test !";
    basicHttpEntity.setContent(new ByteArrayInputStream(testContentAsString.getBytes(StandardCharsets.UTF_8)));
    input.setEntity(basicHttpEntity);
    MockHttpServletResponse output = new MockHttpServletResponse();

    trialverseIOUtilsService.writeResponseContentToServletResponse(input, output);

    assertEquals(testContentAsString, output.getContentAsString());
  }

  @Test
  public void testWriteModelToServletResponse() throws UnsupportedEncodingException {
    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
    Model model = mock(Model.class);
    Graph graph = GraphFactory.createGraphMem();
    Triple triple = new Triple(NodeFactory.createURI("http://test.com/asd"), NodeFactory.createURI("http://something"), NodeFactory.createLiteral("c"));
    graph.add(triple);
    when(model.getGraph()).thenReturn(graph);
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);

    String expexted = "{\n" +
            "  \"@id\" : \"http://test.com/asd\",\n" +
            "  \"something\" : \"c\",\n" +
            "  \"@context\" : {\n" +
            "    \"something\" : \"http://something\"\n" +
            "  }\n" +
            "}\n";

    assertEquals(expexted, httpServletResponse.getContentAsString());
  }
}
