package org.drugis.addis.trialverse.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

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
  RestOperationsFactory restOperationsFactory;


  @Override
  public Collection<Namespace> queryNameSpaces() {
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
      "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "\n" +
      "SELECT ?dataset ?label ?comment WHERE {\n" +
      "  GRAPH ?dataset {\n" +
      "    ?dataset a ontology:Dataset .\n" +
      "    ?dataset rdfs:label ?label .\n" +
      "    ?dataset rdfs:comment ?comment .\n" +
      "  }\n" +
      "}\n";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<Namespace> namespaces = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.dataset.value");
      uid = uid.split(":")[1]; // expected: "dataset:12345-2345-2346-12345etc"
      String name = JsonPath.read(binding, "$.label.value");
      String description = JsonPath.read(binding, "$.comment.value");
      namespaces.add(new Namespace(uid, name, description));
    }
    return namespaces;
  }

  @Override
  public Namespace getNamespace(String uid) {
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
      "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "\n" +
      "SELECT ?dataset ?label ?comment WHERE {\n" +
      "  GRAPH dataset:" + uid + " {\n" +
      "    ?dataset a ontology:Dataset .\n" +
      "    ?dataset rdfs:label ?label .\n" +
      "    ?dataset rdfs:comment ?comment .\n" +
      "  }\n" +
      "}\n";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Object binding = bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    String description = JsonPath.read(binding, "$.comment.value");
    return new Namespace(uid, name, description);
  }


  @Override
  public List<SemanticOutcome> getOutcomes(String namespaceUid) {
    List<SemanticOutcome> outcomes = new ArrayList<>();

    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
      "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "\n" +
      "PREFIX entity: <http://trials.drugis.org/entities/> \n" +
      "\n" +
      "SELECT ?outcome ?label WHERE {\n" +
      "  GRAPH dataset:" + namespaceUid + " {\n" +
      "    { ?outcome rdfs:subClassOf ontology:Endpoint } UNION { ?outcome rdfs:subClassOf ontology:AdverseEvent } .\n" +
      "    ?outcome rdfs:label ?label .\n" +
      "  }\n" +
      "}\n";
    System.out.println(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.outcome.value");
      uid = uid.split(":")[1]; // expected: "entity:12345-2345-2346-12345etc"
      String label = JsonPath.read(binding, "$.label.value");
      outcomes.add(new SemanticOutcome(uid, label));
    }
    return outcomes;
  }

  @Override
  public List<SemanticIntervention> getInterventions(String namespaceUid) {
    List<SemanticIntervention> interventions = new ArrayList<>();

    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
      "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "\n" +
      "PREFIX entity: <http://trials.drugis.org/entities/> \n" +
      "\n" +
      "SELECT ?intervention ?label WHERE {\n" +
      "  GRAPH dataset:" + namespaceUid + " {\n" +
      "    ?intervention rdfs:subClassOf ontology:Drug .\n" +
      "    ?intervention rdfs:label ?label .\n" +
      "  }\n" +
      "}\n";

    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.intervention.value");
      uid = uid.split(":")[1]; // expected: "entity:12345-2345-2346-12345etc"
      String label = JsonPath.read(binding, "$.label.value");
      interventions.add(new SemanticIntervention(uid, label));
    }
    return interventions;
  }

  @Override
  public List<Study> queryStudies(String namespaceUid) {
    List<Study> studies = new ArrayList<>();
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
      "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
      "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
      "\n" +
      "PREFIX study: <http://trials.drugis.org/studies/>\n" +
      "\n" +
      "SELECT ?study ?title ?label WHERE {\n" +
      "  GRAPH dataset:" + namespaceUid + " {\n" +
      "    ?dataset ontology:contains_study ?study .\n" +
      "  }\n" +
      "  GRAPH ?study {\n" +
      "    ?study rdfs:label ?label .\n" +
      "    ?study rdfs:comment ?title .\n" +
      "  }\n" +
      "}";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.study.value");
      uid = uid.split(":")[1];// "expected return: study:af0d9-adf0-rtwe-etc"
      String name = JsonPath.read(binding, "$.label.value");
      String title = JsonPath.read(binding, "$.comment.value");
      studies.add(new Study(uid, name, title));
    }
    return studies;
  }

  @Override
  public Map<Long, String> getTrialverseDrugs(String namespaceUid, String studyUid, Collection<String> drugURIs) {
    String optionString = buildOptionStringFromConceptURIs(drugURIs);
    String query1 = "TODO";
    System.out.println(query1);
    String query = query1;

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

  public List<Pair<Long, Long>> getOutcomeVariableUidsByStudyForSingleOutcome(String namespaceUid, List<String> studyUids, String outcomeURI) {
    String query = "TODO";

    System.out.println("getOutcomeVariableUidsByStudyForSingleOutcome query: " + query);
    String response = queryTripleStore(query);
    System.out.println("getOutcomeVariableUidsByStudyForSingleOutcome response: " + response);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<Pair<Long, Long>> studyVariablesForOutcome = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      Long studyId = findStudyIdInURI(uri);
      Long variableId = Long.valueOf(subStringAfterLastSlash(uri));
      studyVariablesForOutcome.add(Pair.of(studyId, variableId));
    }
    return studyVariablesForOutcome;
  }

  @Override
  public Map<Long, String> getTrialverseVariables(String namespaceUid, Long studyId, Collection<String> outcomeURIs) {
    String optionString = buildOptionStringFromConceptURIs(outcomeURIs);
    String query1 = "TODO";
    System.out.println(query1);
    String query = query1;

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

  @Override
  public Map<String, List<TrialDataIntervention>> findStudyInterventions(String namespaceUid, List<String> studyUids, List<String> interventionURIs) {
    String conceptOptionsString = buildOptionStringFromConceptURIs(interventionURIs);
    String studyOptionsString = StringUtils.join(studyUids, "|");
    String query = "TODO";
    System.out.println(query);
    String response = queryTripleStore(query);

    Map<String, List<TrialDataIntervention>> studyInterventionsMap = new HashMap<>();
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");

    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String semanticInterventionUri = JsonPath.read(binding, "$.type.value");
      String studyUid = ""; // FIXME
      String drugUid = ""; //FIXME;
      TrialDataIntervention trialDataIntervention = new TrialDataIntervention(drugUid, semanticInterventionUri, studyUid);

      List<TrialDataIntervention> interventions = studyInterventionsMap.get(studyUid);
      if (interventions == null) {
        interventions = new ArrayList<>();
        studyInterventionsMap.put(studyUid, interventions);
      }
      interventions.add(trialDataIntervention);
    }

    return studyInterventionsMap;
  }

  private String queryTripleStore(String query) {
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");
    return restOperationsFactory.build().getForObject(triplestoreUri + "?query={query}&output={output}", String.class, vars);
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

  private String buildOptionStringFromIds(List<String> ids) {
    return StringUtils.join(ids, "|");
  }

  public List<Long> findStudiesReferringToConcept(String namespaceUid, String conceptUri) {
    List<Long> studyIds = new ArrayList<>();
    String query =
      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "\n" +
        "SELECT * WHERE {\n" +
        " GRAPH <http://trials.drugis.org/namespaces/" + namespaceUid + "/> {\n" +
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
