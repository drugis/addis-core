package org.drugis.addis.trialverse.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {

  private final static String triplestoreUri = System.getenv("TRIPLESTORE_URI");
  private final static Pattern STUDY_ID_FROM_URI_PATTERN = Pattern.compile("http://trials.drugis.org/study/(\\d+)/.*");

  @Inject
  RestTemplate triplestoreTemplate;

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

  @Override
  public List<SemanticOutcome> getOutcomes(Long namespaceId) {
    List<SemanticOutcome> outcomes = new ArrayList<>();

    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "SELECT  * WHERE {\n" +
            "GRAPH <http://trials.drugis.org/namespaces/" + namespaceId + "/> {\n" +
            "    ?uri rdfs:label ?label .\n" +
            "    FILTER regex (str(?uri), \"namespaces/"
            + namespaceId +
            "/(endpoint|adverseEvent)\", \"i\")\n" +
            "  }\n" +
            "}";
    System.out.println(query);
    String response = queryTripleStore(query);
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
            "GRAPH <http://trials.drugis.org/namespaces/" + namespaceId + "/> {\n" +
            "    ?uri rdfs:label ?label .\n" +
            "    FILTER regex (str(?uri), \"namespaces/"
            + namespaceId +
            "/(drug)\", \"i\")\n" +
            "  }\n" +
            "}";
    System.out.println(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      interventions.add(new SemanticIntervention((String) JsonPath.read(binding, "$.uri.value"),
              (String) JsonPath.read(binding, "$.label.value")));
    }
    return interventions;
  }

  @Override
  public Map<Long, String> getTrialverseDrugs(Long namespaceId, Long studyId, Collection<String> drugURIs) {
    return getTrialverseConceptIds(namespaceId, studyId, AnalysisConcept.DRUG, drugURIs);
  }

  @Override
  public Map<Long, String> getTrialverseVariables(Long namespaceId, Long studyId, Collection<String> outcomeURIs) {
    return getTrialverseConceptIds(namespaceId, studyId, AnalysisConcept.OUTCOME, outcomeURIs);
  }

  @Override
  public Map<Long, List<Pair<Long, String>>> findStudyInterventions(Long namespaceId, List<Long> studyIds, List<String> interventionURIs) {
    AnalysisConcept drugConcept = AnalysisConcept.DRUG;
    String conceptOptionsString = buildOptionStringFromConceptURIs(interventionURIs);
    String studyOptionsString = StringUtils.join(studyIds, "|");
    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "SELECT  * WHERE {\n" +
            " GRAPH <http://trials.drugis.org/namespaces/" + namespaceId + "/> {\n" +
            "   ?uri rdf:type ?type .\n" +
            "   FILTER regex(str(?type), \"namespaces/" +
            namespaceId + "/" + drugConcept.getSearchString() + "/(" + conceptOptionsString + ")\") .\n" +
            "   FILTER regex(str(?uri), \"/study/(" + studyOptionsString + ")\") .\n" +
            " }\n" +
            "}";
    System.out.println(query);

    String response = queryTripleStore(query);
    System.out.println("!!!!!!! AND the responce is: " + response);

    Map<Long, List<Pair<Long, String>>> studyInterventions = new HashMap<>();
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String semanticInterventionUri = JsonPath.read(binding, "$.type.value");
      Long studyId = findStudyIdInURI(uri);
      List<Pair<Long, String>> interventions = studyInterventions.get(studyId);
      if (interventions == null) {
        interventions = new ArrayList<>();
        studyInterventions.put(studyId, interventions);
      }
      Long interventionId = Long.valueOf(subStringAfterLastSlash(uri));
      interventions.add(Pair.of(interventionId, semanticInterventionUri));
    }

    return studyInterventions;
  }

  private Map<Long, String> getTrialverseConceptIds(Long namespaceId, Long studyId, AnalysisConcept analysisConcept, Collection<String> conceptURIs) {
    String optionString = buildOptionStringFromConceptURIs(conceptURIs);
    String query = createFindUsagesQuery(namespaceId, studyId, analysisConcept, optionString);

    String response = queryTripleStore(query);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Map<Long, String> concepts = new HashMap<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String typeUri = JsonPath.read(binding, "$.type.value");
      Long conceptId = extractConceptIdFromUri(uri);
      concepts.put(conceptId, typeUri);
    }
    return concepts;
  }

  private String queryTripleStore(String query) {
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");
    return triplestoreTemplate.getForObject(triplestoreUri + "?query={query}&output={output}", String.class, vars);
  }

  private String buildOptionStringFromConceptURIs(Collection<String> conceptURIs) {
    Collection<String> strippedUris = Collections2.transform(conceptURIs, new Function<String, String>() {
      @Override
      public String apply(String s) {
        return subStringAfterLastSlash(s);
      }
    });
    return StringUtils.join(strippedUris, "|");
  }

  private String createFindUsagesQuery(Long namespaceId, Long studyId, AnalysisConcept analysisConcept, String URIsToFind) {
    String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "SELECT  * WHERE {\n" +
            " GRAPH <http://trials.drugis.org/namespaces/" + namespaceId + "/> {\n" +
            "   ?uri rdf:type ?type .\n" +
            "   FILTER regex(str(?type), \"namespaces/" +
            namespaceId + "/" + analysisConcept.getSearchString() + "/(" + URIsToFind + ")\") .\n" +
            "   FILTER regex(str(?uri), \"/study/" + studyId + "\") .\n" +
            " }\n" +
            "}";
    System.out.println(query);
    return query;
  }

  public List<Long> findStudiesReferringToConcept(Long namespaceId, String conceptUri) {
    List<Long> studyIds = new ArrayList<>();
    String query =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "\n" +
                    "SELECT * WHERE {\n" +
                    " GRAPH <http://trials.drugis.org/namespaces/" + namespaceId + "/> {\n" +
                    "   ?uri rdf:type <" + conceptUri + "> .\n" +
                    " }\n" +
                    "}";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");

    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      Long studyId = findStudyIdInURI(uri);
      studyIds.add(studyId);
    }

    return studyIds;
  }

  private Long findStudyIdInURI(String uri) {
    // extract numerical study id
    Matcher matcher = STUDY_ID_FROM_URI_PATTERN.matcher(uri);
    matcher.find();
    return (Long.valueOf(matcher.group(1)));
  }

  private Long extractConceptIdFromUri(String uri) {
    return Long.parseLong(subStringAfterLastSlash(uri));
  }

  private String subStringAfterLastSlash(String inStr) {
    return inStr.substring(inStr.lastIndexOf("/") + 1);
  }

}
