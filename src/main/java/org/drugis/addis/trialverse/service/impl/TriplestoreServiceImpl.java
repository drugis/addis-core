package org.drugis.addis.trialverse.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {

  public enum AnalysisConcept {
    DRUG("drug"),
    OUTCOME("(adverseEvent|endpoint)");
    private final String searchString;

    AnalysisConcept(String searchString) {
      this.searchString = searchString;
    }
    public String getSearchString() {
      return this.searchString;
    }
  }

  final String triplestoreUri = System.getenv("TRIPLESTORE_URI");

  @Inject
  RestTemplate triplestoreTemplate;

  @Override
  public List<SemanticOutcome> getOutcomes(Long namespaceId) {
    List<SemanticOutcome> outcomes = new ArrayList<>();

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
    for (Object binding : bindings) {
      outcomes.add(new SemanticOutcome((String) JsonPath.read(binding, "$.uri.value"),
        (String) JsonPath.read(binding, "$.label.value")));
    }
    return outcomes;
  }

  @Override
  public List<SemanticIntervention> getInterventions(Long namespaceId) {
    List<SemanticIntervention> interventions = new ArrayList<>();

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
    for (Object binding : bindings) {
      interventions.add(new SemanticIntervention((String) JsonPath.read(binding, "$.uri.value"),
        (String) JsonPath.read(binding, "$.label.value")));
    }
    return interventions;
  }

  @Override
  public List<Long> getTrialverseDrugIds(Integer namespaceId, Integer studyId, List<String> drugURIs) {
    return getTrialverseConceptIds(namespaceId, studyId, AnalysisConcept.DRUG, drugURIs);
  }

  @Override
  public List<Long> getTrialverseOutcomeIds(Integer namespaceId, Integer studyId, List<String> outcomeURIs) {
    return getTrialverseConceptIds(namespaceId, studyId, AnalysisConcept.OUTCOME, outcomeURIs);
  }

  private List<Long> getTrialverseConceptIds(Integer namespaceId, Integer studyId, AnalysisConcept analysisConcept, List<String> conceptURIs) {
    Collection<String> strippedUris = Collections2.transform(conceptURIs, new Function<String, String>() {
      @Override
      public String apply(String s) {
        return subStringAfterLastSlash(s);
      }
    });
    String uriOptions = StringUtils.join(strippedUris, "|");

    String query = createFindUsagesQuery(namespaceId, studyId, AnalysisConcept.DRUG, uriOptions);

    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");

    String response = triplestoreTemplate.getForObject(triplestoreUri + "?query={query}&output={output}", String.class, vars);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<Long> conceptIds = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      Long conceptId = extractConceptIdFromUri(uri);
      conceptIds.add(conceptId);
    }
    return conceptIds;
  }

  private String createFindUsagesQuery(Integer namespaceId, Integer studyId, AnalysisConcept analysisConcept, String URIsToFind) {
    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
      "\n" +
      "SELECT  * WHERE {\n" +
      " GRAPH <http://trials.drugis.org/> {\n" +
      "   ?uri rdf:type ?type .\n" +
      "   FILTER regex(str(?type), \"namespace/" +
      namespaceId + "/" + analysisConcept.getSearchString() +  "/(" + URIsToFind + ")\") .\n" +
      "   FILTER regex(str(?uri), \"/study/" + studyId + "\") .\n" +
      " }\n" +
      "}";
    System.out.println(query);
    return query;
  }

  private Long extractConceptIdFromUri(String uri) {
    return Long.parseLong(subStringAfterLastSlash(uri));
  }

  private String subStringAfterLastSlash(String inStr) {
    return inStr.substring(inStr.lastIndexOf("/") + 1);
  }

}
