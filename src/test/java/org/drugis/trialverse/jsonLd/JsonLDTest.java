package org.drugis.trialverse.jsonLd;

import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;

import static org.drugis.trialverse.util.Utils.loadResource;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by daan on 18-9-15.
 */
public class JsonLDTest {

  @Test
  public void jenaBugTest() throws IOException {
    Model model = ModelFactory.createDefaultModel();
    model.read(new FileReader("src/test/resources/jenaBugTest.ttl"), "http://example.com", RDFLanguages.strLangTurtle);

    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, RDFLanguages.JSONLD);
    writer.close();

    Model model2 = ModelFactory.createDefaultModel();
    model2.read(new StringReader(writer.toString()), "http://example.com", RDFLanguages.strLangJSONLD);

    assertTrue(model.isIsomorphicWith(model2));
  }

  @Ignore
  @Test
  public void testTurtleRoundtrip() throws IOException, ScriptException {
    String exampleJsonLd = loadResource(this.getClass(), "/minimalList.json");

    Model model = ModelFactory.createDefaultModel();
    model.read(new StringReader(exampleJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    RDFDataMgr.write(outputStream, model, Lang.TURTLE);
    String turtleStringModel = outputStream.toString();

    Model model1a = ModelFactory.createDefaultModel();
    model1a.read(new StringReader(turtleStringModel), "http://example.com", RDFLanguages.strLangTurtle);

    if (!model.isIsomorphicWith(model1a)) {
      Delta d = new Delta(model.getGraph());
      d.clear();
      GraphUtil.addInto(d, model1a.getGraph());
      System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      RDFDataMgr.write(System.err, d.getAdditions(), Lang.TURTLE);
      System.err.println("-------------------------------------------------------------------------------------");
      RDFDataMgr.write(System.err, d.getDeletions(), Lang.TURTLE);
      fail("!!!!!!!!!!!!!!!!!!!!!! RDF not equal");
    }

  }

  @Test
  public void testTransformIsomorphy() throws IOException, ScriptException {
    String exampleJsonLd = loadResource(this.getClass(), "/jenaEsExampleJsonLd.json");
    String underscoreLoc = "src/test/java/org/drugis/trialverse/jsonLd/scripts/lodash-4.2.1.js";
    String contextLoc = "src/main/webapp/resources/app/js/util/context.js";
    String transformScriptLoc = "src/main/webapp/resources/app/js/util/transformJsonLd.js";

    Model model = ModelFactory.createDefaultModel();
    model.read(new StringReader(exampleJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);

    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    engine.put("inputJson", exampleJsonLd);
    engine.eval(new FileReader(underscoreLoc));
    engine.eval("var context; function define(deps, def) { context = def(); }"); // stub requireJS
    engine.eval(new FileReader(contextLoc));
    engine.eval("var improveJsonLd; function define(deps, def) { improveJsonLd = def(_, context); }"); // stub requireJS
    engine.eval(new FileReader(transformScriptLoc));
    engine.eval("var outputJson = JSON.stringify(improveJsonLd(JSON.parse(inputJson)), null, 2)");
    String betterJsonLd = (String) engine.get("outputJson");

    Model model2 = ModelFactory.createDefaultModel();
    model2.read(new StringReader(betterJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);

    if (!model.isIsomorphicWith(model2)) {
      Delta d = new Delta(model.getGraph());
      d.clear();
      GraphUtil.addInto(d, model2.getGraph());
      System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      RDFDataMgr.write(System.err, d.getAdditions(), Lang.TURTLE);
      System.err.println("-------------------------------------------------------------------------------------");
      RDFDataMgr.write(System.err, d.getDeletions(), Lang.TURTLE);
      fail("!!!!!!!!!!!!!!!!!!!!!! RDF not equal");
    }

  }


}
