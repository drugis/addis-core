package org.drugis.trialverse.jsonLd;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
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

  @Test
  public void testJsonLD() throws IOException, ScriptException {
    String exampleJsonLd = loadResource(this.getClass(), "/exampleJsonLd.json");
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

    Model model2 = ModelFactory.createDefaultModel();
    model2.read(new StringReader(betterJsonLd), "http://example.com", RDFLanguages.strLangJSONLD);

    assertTrue(model.isIsomorphicWith(model2));

  }

}
