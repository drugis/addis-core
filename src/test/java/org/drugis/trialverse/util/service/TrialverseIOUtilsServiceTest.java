package org.drugis.trialverse.util.service;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.drugis.trialverse.util.service.impl.TrialverseIOUtilsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

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

    MockHttpServletResponse output = new MockHttpServletResponse();

    trialverseIOUtilsService.writeContentToServletResponse(testContentAsString.getBytes(), output);

    assertEquals(testContentAsString, output.getContentAsString());
  }

  @Test
  public void testWriteModelToServletResponse() throws UnsupportedEncodingException {
    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
    Graph graph = GraphFactory.createGraphMem();
    Triple triple = new Triple(NodeFactory.createURI("http://test.com/asd"), NodeFactory.createURI("http://something"), NodeFactory.createLiteral("c"));
    graph.add(triple);
    Model model = ModelFactory.createModelForGraph(graph);
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);

    String expected = "<http://test.com/asd>\n" +
            "        <http://something>  \"c\" .";

    assertEquals(expected.trim(), httpServletResponse.getContentAsString().trim());
  }

  @Test
  public void testWriteModelToServletResponseJson() throws UnsupportedEncodingException {
    MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
    Graph graph = GraphFactory.createGraphMem();
    Triple triple = new Triple(NodeFactory.createURI("http://test.com/asd"), NodeFactory.createURI("http://something"), NodeFactory.createLiteral("c"));
    graph.add(triple);
    Model model = ModelFactory.createModelForGraph(graph);
    trialverseIOUtilsService.writeModelToServletResponseJson(model, httpServletResponse);

    String expected = "{\n" +
            "  \"@id\" : \"http://test.com/asd\",\n" +
            "  \"http://something\" : \"c\",\n" +
            "  \"@context\" : {\n" +
            "    \"something\" : {\n" +
            "      \"@id\" : \"http://something\",\n" +
            "      \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    assertEquals(expected.trim(), httpServletResponse.getContentAsString().trim());
  }
}
