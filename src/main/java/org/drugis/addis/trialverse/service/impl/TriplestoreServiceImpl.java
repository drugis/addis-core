package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {
  final String triplestoreUri = System.getenv("TRIPLESTORE_URI");

  @Inject
  RestTemplate triplestoreTemplate;

  @Override
  public List<SemanticOutcome> getOutcomes(Long namespaceId) {
    ArrayList<SemanticOutcome> outcomes = new ArrayList<>();

    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "SELECT  * WHERE {\n" +
            "GRAPH <http://trials.drugis.org/> {\n" +
            "    ?uri rdfs:label ?label .\n" +
            "    FILTER regex (str(?uri), \"namespace/"
            + namespaceId +
            "/(endpoint|adverseEvent)\", \"i\")\n" +
            "  }\n" +
            "}";
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");

    String response = triplestoreTemplate.getForObject(triplestoreUri + "?query={query}&output={output}", String.class, vars);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (int i = 0; i < bindings.size(); ++i) {
      Object binding = bindings.get(i);
      outcomes.add(new SemanticOutcome((String) JsonPath.read(binding, "$.uri.value"),
              (String) JsonPath.read(binding, "$.label.value")));
    }
    return outcomes;
  }

  @Override
  public List<SemanticIntervention> getInterventions(Long namespaceId) {
    ArrayList<SemanticIntervention> interventions = new ArrayList<>();

    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "SELECT  * WHERE {\n" +
            "GRAPH <http://trials.drugis.org/> {\n" +
            "    ?uri rdfs:label ?label .\n" +
            "    FILTER regex (str(?uri), \"namespace/"
            + namespaceId +
            "/(drug)\", \"i\")\n" +
            "  }\n" +
            "}";
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");

    String response = triplestoreTemplate.getForObject(triplestoreUri + "?query={query}&output={output}", String.class, vars);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (int i = 0; i < bindings.size(); ++i) {
      Object binding = bindings.get(i);
      interventions.add(new SemanticIntervention((String) JsonPath.read(binding, "$.uri.value"),
              (String) JsonPath.read(binding, "$.label.value")));
    }
    return interventions;
  }
}
