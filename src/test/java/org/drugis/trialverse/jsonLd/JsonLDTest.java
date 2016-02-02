package org.drugis.trialverse.jsonLd;

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
  public void testJsonLDFromJenaEs() throws IOException, ScriptException {
    String exampleJsonLd = loadResource(this.getClass(), "/jenaEsExampleJsonLd.json");
    String underscoreLoc = "src/main/webapp/resources/app/js/bower_components/lodash/lodash.js";
    String transformScriptLoc = "src/main/webapp/resources/app/js/util/transformJsonLd.js";

    Model model = ModelFactory.createDefaultModel();
    model.read(new StringReader(exampleJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);

    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    engine.put("inputJson", exampleJsonLd);
    engine.eval(new FileReader(underscoreLoc));
    engine.eval("var improveJsonLd; function define(deps, def) { improveJsonLd = def(_); }"); // stub requireJS
    engine.eval(new FileReader(transformScriptLoc));
    engine.eval("var outputJson = JSON.stringify(improveJsonLd(JSON.parse(inputJson)), null, 2)");
    String betterJsonLd = (String) engine.get("outputJson");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    RDFDataMgr.write(outputStream, model, Lang.TURTLE) ;
    String turtleStringModel = outputStream.toString();

    Model model1a = ModelFactory.createDefaultModel();
    model1a.read(new StringReader(turtleStringModel), "http://example.com", RDFLanguages.strLangTurtle);

    Model model2 = ModelFactory.createDefaultModel();
    model2.read(new StringReader(betterJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);
    ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
    RDFDataMgr.write(outputStream2, model2, Lang.TURTLE) ;
    String turtleStringModel2 = outputStream2.toString();
    Model model2a = ModelFactory.createDefaultModel();
    model2a.read(new StringReader(turtleStringModel2), "http://example.com", RDFLanguages.strLangTurtle);

    assertTrue(model1a.isIsomorphicWith(model2a));


  }



}
