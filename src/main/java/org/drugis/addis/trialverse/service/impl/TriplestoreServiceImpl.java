package org.drugis.addis.trialverse.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyAllocationEnum;
import org.drugis.addis.trialverse.model.emun.StudyBlindingEmun;
import org.drugis.addis.trialverse.model.emun.StudyStatusEnum;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

  public final static String STUDY_DATE_FORMAT = "yyyy-MM-dd";

  private final static String triplestoreUri = System.getenv("TRIPLESTORE_URI");
  private final static Pattern STUDY_UID_FROM_URI_PATTERN = Pattern.compile("http://trials.drugis.org/study/(\\w+)/.*");

  @Inject
  RestOperationsFactory restOperationsFactory;


  @Override
  public Collection<Namespace> queryNameSpaces() {
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
            "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "SELECT ?dataset ?label ?comment (COUNT(DISTINCT(?study)) AS ?numberOfStudies) WHERE {\n" +
            "  GRAPH ?dataset {\n" +
            "    ?dataset a ontology:Dataset .\n" +
            "    ?dataset rdfs:label ?label .\n" +
            "    ?dataset rdfs:comment ?comment .\n" +
            "    ?dataset ontology:contains_study ?study" +
            "  }\n" +
            "} GROUP BY ?dataset ?label ?comment\n";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<Namespace> namespaces = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.dataset.value");
      uid = subStringAfterLastSymbol(uid, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String description = JsonPath.read(binding, "$.comment.value");
      Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
      String sourceUrl = "TODO"; //FIXME
      namespaces.add(new Namespace(uid, name, description, numberOfStudies, sourceUrl));
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
            "SELECT ?dataset ?label ?comment (COUNT(DISTINCT(?study)) AS ?numberOfStudies) WHERE {\n" +
            "  GRAPH dataset:" + uid + " {\n" +
            "    ?dataset a ontology:Dataset .\n" +
            "    ?dataset rdfs:label ?label .\n" +
            "    ?dataset rdfs:comment ?comment .\n" +
            "  }\n" +
            "} GROUP BY ?dataset ?label ?comment\n";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Object binding = bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    String description = JsonPath.read(binding, "$.comment.value");
    Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
    String sourceUrl = "TODO"; //FIXME
    return new Namespace(uid, name, description, numberOfStudies, sourceUrl);
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
      uid = subStringAfterLastSymbol(uid, '/');
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
      uid = subStringAfterLastSymbol(uid, '/');
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
      uid = subStringAfterLastSymbol(uid, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String title = JsonPath.read(binding, "$.title.value");
      studies.add(new Study(uid, name, title));
    }
    return studies;
  }

  @Override
  public List<StudyWithDetails> queryStudydetails(String namespaceUid) {
    List<StudyWithDetails> studiesWithDetail = new ArrayList<>();
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
            "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "\n" +
            "PREFIX study: <http://trials.drugis.org/studies/>\n" +
            "\n" +
            "SELECT ?study ?title ?label ?studySize ?allocation ?blinding ?objective ?drugNames ?inclusionCriteria" +
            " ?publications ?status ?numberOfCenters ?indication ?startDate ?endDate ?numberOfArms ?doseType WHERE {\n" +
            "  GRAPH dataset:" + namespaceUid + " {\n" +
            "    ?dataset ontology:contains_study ?study .\n" +
            "  }\n" +
            "GRAPH ?study {\n" +
            "    ?study \n" +
            "      rdfs:label ?label ;\n" +
            "      rdfs:comment ?title .\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_allocation ?allocation .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_blinding ?blinding .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_objective [\n" +
            "        rdfs:comment ?objective\n" +
            "      ] .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_eligibility_criteria [\n" +
            "        rdfs:comment ?inclusionCriteria\n" +
            "      ] .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:status ?status .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_number_of_centers ?numberOfCenters .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_indication ?indication_instance .\n" +
            "      ?indication_instance rdfs:label ?indication .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_start_date ?startDate .\n" +
            "    }\n" +
            "    OPTIONAL {\n" +
            "      ?study ontology:has_end_date ?endDate .\n" +
            "    }\n" +
            "      OPTIONAL {\n" +
            "         SELECT ?study ?doseType\n" +
            "           WHERE {\n" +
            "            BIND ('Flexible' as ?doseType)\n" +
            "             ?activity a ontology:TreatmentActivity ;\n" +
            "               ontology:activity_application [\n" +
            "                 ontology:applied_to_arm ?arm \n" +
            "               ] ;\n" +
            "               ontology:administered_drugs/rdf:rest*/rdf:first [ a ontology:TitratedDoseDrugTreatment ] .\n" +
            "               ?study ontology:has_arm ?arm .\n" +
            "            } GROUP BY ?study ?doseType\n" +
            "              HAVING (COUNT(*) > 0)\n" +
            "      }\n" +
            "    OPTIONAL\n" +
            "    {\n" +
            "      SELECT ?study (group_concat(?drugName; separator = \", \") as ?drugNames)\n" +
            "      WHERE {\n" +
            "        GRAPH ?dataset {\n" +
            "          ?drug rdfs:subClassOf ontology:Drug .\n" +
            "          ?dataset ontology:contains_study ?study \n" +
            "        }\n" +
            "        GRAPH ?study {\n" +
            "          ?instance a ?drug .\n" +
            "          ?instance rdfs:label ?drugName.\n" +
            "        }\n" +
            "      } GROUP BY ?study\n" +
            "    }\n" +
            "    OPTIONAL\n" +
            "    {\n" +
            "      SELECT ?study (group_concat(?publication; separator = \", \") as ?publications)\n" +
            "      WHERE {\n" +
            "        GRAPH ?study {\n" +
            "          OPTIONAL {\n" +
            "            ?study ontology:has_publication [\n" +
            "              ontology:has_id ?publication\n" +
            "            ] .\n" +
            "          }\n" +
            "        }\n" +
            "      } GROUP BY ?study\n" +
            "    }\n" +
            "    {\n" +
            "      SELECT ?study (COUNT(?arm) as ?numberOfArms) (SUM(?numberOfParticipantsStarting) as ?studySize)\n" +
            "        WHERE {\n" +
            "          GRAPH ?study {\n" +
            "            ?arm a ontology:Arm .\n" +
            "            ?participantsStarting ontology:of_arm ?arm .\n" +
            "            ?participantsStarting ontology:participants_starting ?numberOfParticipantsStarting .\n" +
            "          }\n" +
            "        } GROUP BY ?study\n" +
            "    }\n" +
            "  }\n" +
            "}";
    System.out.println(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      JSONObject row = (net.minidev.json.JSONObject) binding;
      String uid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.study.value"), '/');
      String name = row.containsKey("label") ? JsonPath.<String>read(binding, "$.label.value") : null;
      String title = row.containsKey("title") ? JsonPath.<String>read(binding, "$.title.value") : null;
      Integer studySize = row.containsKey("studySize") ? Integer.parseInt(JsonPath.<String>read(binding, "$.studySize.value")) : null;
      String allocation = row.containsKey("allocation") ? StudyAllocationEnum.fromString(subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.allocation.value"), '#')).toString() : null;
      String blinding = row.containsKey("blinding") ? StudyBlindingEmun.fromString(subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.blinding.value"), '#')).toString() : null;
      String inclusionCriteria = row.containsKey("inclusionCriteria") ? JsonPath.<String>read(binding, "$.inclusionCriteria.value") : null;
      Integer numberOfStudyCenters = row.containsKey("numberOfCenters") ? Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfCenters.value")) : null;
      String publicationURLs = row.containsKey("publications") ? JsonPath.<String>read(binding, "$.publications.value") : null;
      String status = row.containsKey("status") ? StudyStatusEnum.fromString(subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.status.value"), '#')).toString() : null;
      String indication = row.containsKey("indication") ? JsonPath.<String>read(binding, "$.indication.value") : null;
      String objective = row.containsKey("objective") ? JsonPath.<String>read(binding, "$.objective.value") : null;
      String investigationalDrugNames = row.containsKey("drugNames") ? JsonPath.<String>read(binding, "$.drugNames.value") : null;
      Integer numberOfArms = row.containsKey("numberOfArms") ? Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfArms.value")) : null;

      DateTimeFormatter formatter = DateTimeFormat.forPattern(STUDY_DATE_FORMAT);
      DateTime startDate = row.containsKey("startDate") ? formatter.parseDateTime(JsonPath.<String>read(binding, "$.startDate.value")).toDateMidnight().toDateTime() : null;
      DateTime endDate = row.containsKey("endDate") ? formatter.parseDateTime(JsonPath.<String>read(binding, "$.endDate.value")).toDateMidnight().toDateTime() : null;

      String dosing = row.containsKey("doseType") ? JsonPath.<String>read(binding, "$.doseType.value") : "Fixed"; //todo needs better way of querying

      StudyWithDetails studyWithDetails = new StudyWithDetails
              .StudyWithDetailsBuilder()
              .studyUid(uid)
              .name(name)
              .title(title)
              .studySize(studySize)
              .allocation(allocation)
              .blinding(blinding)
              .inclusionCriteria(inclusionCriteria)
              .numberOfStudyCenters(numberOfStudyCenters)
              .pubmedUrls(publicationURLs)
              .status(status)
              .indication(indication)
              .objectives(objective)
              .investigationalDrugNames(investigationalDrugNames)
              .startDate(startDate)
              .endDate(endDate)
              .numberOfArms(numberOfArms)
              .dosing(dosing)
              .build();
      studiesWithDetail.add(studyWithDetails);
    }
    return studiesWithDetail;
  }

  private String buildInterventionUnionString(List<String> interventionUids) {
    String result = "";
    for (String interventionUid : interventionUids) {
      result = result + " { ?interventionInstance a entity:" + interventionUid + " } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  private String buildOutcomeUnionString(List<String> outcomeUids) {
    String result = "";
    for (String outcomeUid : outcomeUids) {
      result = result + " { ?outcomeInstance a entity:" + outcomeUid + " } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  @Override
  public List<TrialDataStudy> getTrialData(String namespaceUid, String outcomeUid, List<String> interventionUids) {
    String interventionUnion = buildInterventionUnionString(interventionUids);
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
            "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
            "\n" +
            "PREFIX entity: <http://trials.drugis.org/entities/>\n" +
            "PREFIX instance: <http://trials.drugis.org/instances/>\n" +
            "PREFIX study: <http://trials.drugis.org/studies/>\n" +
            "\n" +
            "SELECT DISTINCT ?study ?studyName ?drug ?interventionLabel ?drugInstance ?outcomeInstance ?outcomeTypeUid ?outcomeInstanceLabel ?arm ?armLabel ?mean ?stdDev ?count ?sampleSize WHERE {\n" +
            "  GRAPH ?dataset {\n" +
            "    ?dataset a ontology:Dataset .\n" +
            "    ?dataset ontology:contains_study ?study .\n" +
            "  }\n" +
            "  GRAPH ?study {\n" +
            "    ?study rdfs:label ?studyName .\n" +
            "    ?study ontology:has_outcome ?outcomeInstance .\n" +
            "    ?outcomeInstance a entity:" + outcomeUid + " .\n" +
            "    ?outcomeInstance a ?outcomeTypeUid .\n" +
            "    ?outcomeInstance rdfs:label ?outcomeInstanceLabel .\n" +
            interventionUnion +
            "  }\n" +
            "  GRAPH ?study {\n" +
            "    ?drugInstance a ?drug .\n" +
            "    ?drugInstance rdfs:label ?interventionLabel .\n" +
            "    ?study ontology:has_arm ?arm .\n" +
            "    ?study ontology:has_primary_epoch ?epoch .\n" +
            "    ?activity a ontology:TreatmentActivity ;\n" +
            "      ontology:activity_application [\n" +
            "        ontology:applied_to_arm ?arm ;\n" +
            "        ontology:applied_in_epoch ?epoch\n" +
            "      ] ;\n" +
            "      ontology:administered_drugs ([ ontology:treatment_has_drug ?drugInstance ]) .\n" +
            "\n" +
            "    ?epoch rdfs:label ?epochLabel .\n" +
            "    ?arm rdfs:label ?armLabel .\n" +
            "\n" +
            "    # also get the measurement while we're here\n" +
            "    ?measurementMoment\n" +
            "      ontology:relative_to_epoch ?epoch ;\n" +
            "      ontology:relative_to_anchor ontology:anchorEpochEnd ;\n" +
            "      ontology:time_offset \"-P0D\"^^xsd:duration .\n" +
            "\n" +
            "    ?measurement\n" +
            "      ontology:of_moment ?measurementMoment ;\n" +
            "      ontology:of_outcome ?outcomeInstance ;\n" +
            "      ontology:of_arm ?arm .\n" +
            "\n" +
            "    OPTIONAL {\n" +
            "      ?measurement\n" +
            "        ontology:mean ?mean ;\n" +
            "        ontology:standard_deviation ?stdDev ;\n" +
            "        ontology:sample_size ?sampleSize .\n" +
            "    }\n" +
            "    \n" +
            "    OPTIONAL {\n" +
            "      ?measurement\n" +
            "        ontology:count ?count ;\n" +
            "        ontology:sample_size ?sampleSize .\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
    String response = queryTripleStore(query);
    System.out.println(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Map<String, TrialDataStudy> trialDataStudies = new HashMap<>();
    // ?studyName ?drug ?interventionLabel ?interventionInstance ?outcomeInstance ?outcomeTypeUid ?outcomeInstanceLabel ?arm ?armLabel ?mean ?stdDev ?count ?sampleSize WHERE {
    for (Object binding : bindings) {
      String studyUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.study.value"), '/');
      TrialDataStudy trialDataStudy = trialDataStudies.get(studyUid);
      if (trialDataStudy == null) {
        String studyName = JsonPath.read(binding, "$.studyName.value");
        trialDataStudy = new TrialDataStudy(studyUid, studyName, new ArrayList<TrialDataIntervention>(), new ArrayList<TrialDataArm>());
        trialDataStudies.put(studyUid, trialDataStudy);
      }
      String drugInstanceUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.drugInstance.value"), '/');
      String drugUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.drug.value"), '/');
      TrialDataIntervention trialDataIntervention = new TrialDataIntervention(drugInstanceUid, drugUid, studyUid);
      trialDataStudy.getTrialDataInterventions().add(trialDataIntervention);

      Double mean = null;
      Double stdDev = null;
      Long rate = null;
      JSONObject bindingObject = (JSONObject) binding;
      Boolean isContinuous = bindingObject.containsKey("mean");
      if (isContinuous) {
        mean = Double.parseDouble(JsonPath.<String>read(binding, "$.mean.value"));
        stdDev = Double.parseDouble(JsonPath.<String>read(binding, "$.stdDev.value"));
      } else {
        rate = Long.parseLong(JsonPath.<String>read(binding, "$.count.value"));
      }
      Long sampleSize = Long.parseLong(JsonPath.<String>read(binding, "$.sampleSize.value"));
      String armUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.arm.value"), '/');
      String armLabel = JsonPath.read(binding, "$.armLabel.value");
      String variableUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.outcomeInstance.value"), '/');
      Measurement measurement = new Measurement(studyUid, variableUid, armUid, sampleSize, rate, stdDev, mean);
      TrialDataArm trialDataArm = new TrialDataArm(armUid, armLabel, studyUid, drugInstanceUid, drugUid, measurement);
      trialDataStudy.getTrialDataArms().add(trialDataArm);

    }
    return new ArrayList<>(trialDataStudies.values());
  }

  @Override
  public List<SingleStudyBenefitRiskMeasurementRow> getSingleStudyMeasurements(String studyUid, List<String> outcomeUids, List<String> interventionUids) {
    String outcomeUnionString = buildOutcomeUnionString(outcomeUids);
    String interventionUnionString = buildInterventionUnionString(interventionUids);

    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
            "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
            "\n" +
            "PREFIX entity: <http://trials.drugis.org/entities/>\n" +
            "PREFIX instance: <http://trials.drugis.org/instances/>\n" +
            "PREFIX study: <http://trials.drugis.org/studies/>\n" +
            "\n" +
            "SELECT DISTINCT ?interventionTypeUid ?interventionLabel ?outcomeTypeUid ?outcomeInstanceLabel ?mean ?stdDev ?count ?sampleSize WHERE {\n" +
            "  GRAPH ?dataset {\n" +
            "    ?dataset a ontology:Dataset .\n" +
            "    ?dataset ontology:contains_study ?study .\n" +
            "  }\n" +
            "  GRAPH study:" + studyUid + " {\n" +
            "    ?study rdfs:label ?studyName .\n" +
            "    ?study ontology:has_outcome ?outcomeInstance .\n" +
            outcomeUnionString +
            ".\n" +
            "    ?outcomeInstance a ?outcomeTypeUid .\n" +
            "    ?outcomeInstance rdfs:label ?outcomeInstanceLabel .\n" +
            interventionUnionString +
            " .\n" +
            "    ?interventionInstance a ?interventionTypeUid .\n" +
            "  }\n" +
            "  GRAPH ?study {\n" +
            "    ?interventionInstance rdfs:label ?interventionLabel .\n" +
            "    ?study ontology:has_arm ?arm .\n" +
            "    ?study ontology:has_primary_epoch ?epoch .\n" +
            "    ?activity a ontology:TreatmentActivity ;\n" +
            "      ontology:activity_application [\n" +
            "        ontology:applied_to_arm ?arm ;\n" +
            "        ontology:applied_in_epoch ?epoch\n" +
            "      ] ;\n" +
            "      ontology:administered_drugs ([ ontology:treatment_has_drug ?interventionInstance ]) .\n" +
            "\n" +
            "    ?epoch rdfs:label ?epochLabel .\n" +
            "    ?arm rdfs:label ?armLabel .\n" +
            "\n" +
            "    # also get the measurement while we're here\n" +
            "    ?measurementMoment\n" +
            "      ontology:relative_to_epoch ?epoch ;\n" +
            "      ontology:relative_to_anchor ontology:anchorEpochEnd ;\n" +
            "      ontology:time_offset \"-P0D\"^^xsd:duration .\n" +
            "\n" +
            "    ?measurement\n" +
            "      ontology:of_moment ?measurementMoment ;\n" +
            "      ontology:of_outcome ?outcomeInstance ;\n" +
            "      ontology:of_arm ?arm .\n" +
            "\n" +
            "    OPTIONAL {\n" +
            "      ?measurement\n" +
            "        ontology:mean ?mean ;\n" +
            "        ontology:standard_deviation ?stdDev ;\n" +
            "        ontology:sample_size ?sampleSize .\n" +
            "    }\n" +
            "\n" +
            "    OPTIONAL {\n" +
            "      ?measurement\n" +
            "        ontology:count ?count ;\n" +
            "        ontology:sample_size ?sampleSize .\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<SingleStudyBenefitRiskMeasurementRow> measurementObjects = new ArrayList<>();
    for (Object binding : bindings) {
      JSONObject bindingObject = (JSONObject) binding;
      String outcomeUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.outcomeTypeUid.value"), '/');
      String outcomeLabel = JsonPath.read(binding, "$.outcomeInstanceLabel.value");
      String alternativeUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.interventionTypeUid.value"), '/');
      String alternativeLabel = JsonPath.read(binding, "$.interventionLabel.value");
      Double mean = null;
      Double stdDev = null;
      Long rate = null;
      Boolean isContinuous = bindingObject.containsKey("mean");
      if (isContinuous) {
        mean = Double.parseDouble(JsonPath.<String>read(binding, "$.mean.value"));
        stdDev = Double.parseDouble(JsonPath.<String>read(binding, "$.stdDev.value"));
      } else {
        rate = Long.parseLong(JsonPath.<String>read(binding, "$.count.value"));
      }
      Long sampleSize = Long.parseLong(JsonPath.<String>read(binding, "$.sampleSize.value"));
      measurementObjects.add(new SingleStudyBenefitRiskMeasurementRow(outcomeUid, outcomeLabel, alternativeUid, alternativeLabel, mean, stdDev, rate, sampleSize));
    }
    return measurementObjects;
  }

  public static class SingleStudyBenefitRiskMeasurementRow {
    private String outcomeUid;
    private String outcomeLabel;
    private String alternativeUid;
    private String alternativeLabel;
    private Double mean;
    private Double stdDev;
    private Long rate;
    private Long sampleSize;

    public SingleStudyBenefitRiskMeasurementRow(String outcomeUid, String outcomeLabel, String alternativeUid, String alternativeLabel, Double mean, Double stdDev, Long rate, Long sampleSize) {
      this.outcomeUid = outcomeUid;
      this.outcomeLabel = outcomeLabel;
      this.alternativeUid = alternativeUid;
      this.alternativeLabel = alternativeLabel;
      this.mean = mean;
      this.stdDev = stdDev;
      this.rate = rate;
      this.sampleSize = sampleSize;
    }

    public String getOutcomeUid() {
      return outcomeUid;
    }

    public String getOutcomeLabel() {
      return outcomeLabel;
    }

    public String getAlternativeUid() {
      return alternativeUid;
    }

    public String getAlternativeLabel() {
      return alternativeLabel;
    }

    public Double getMean() {
      return mean;
    }

    public Double getStdDev() {
      return stdDev;
    }

    public Long getRate() {
      return rate;
    }

    public Long getSampleSize() {
      return sampleSize;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SingleStudyBenefitRiskMeasurementRow)) return false;

      SingleStudyBenefitRiskMeasurementRow that = (SingleStudyBenefitRiskMeasurementRow) o;

      if (!alternativeLabel.equals(that.alternativeLabel)) return false;
      if (!alternativeUid.equals(that.alternativeUid)) return false;
      if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
      if (!outcomeLabel.equals(that.outcomeLabel)) return false;
      if (!outcomeUid.equals(that.outcomeUid)) return false;
      if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
      if (!sampleSize.equals(that.sampleSize)) return false;
      if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = outcomeUid.hashCode();
      result = 31 * result + outcomeLabel.hashCode();
      result = 31 * result + alternativeUid.hashCode();
      result = 31 * result + alternativeLabel.hashCode();
      result = 31 * result + (mean != null ? mean.hashCode() : 0);
      result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
      result = 31 * result + (rate != null ? rate.hashCode() : 0);
      result = 31 * result + sampleSize.hashCode();
      return result;
    }
  }

  @Override
  public Map<String, String> getTrialverseDrugs(String namespaceUid, String studyUid, Collection<String> drugURIs) {
    String optionString = buildOptionStringFromConceptURIs(drugURIs);
    String query = "TODO";
    System.out.println(query);

    String response = queryTripleStore(query);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Map<String, String> concepts = new HashMap<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String typeUri = JsonPath.read(binding, "$.type.value");
      String conceptId = subStringAfterLastSymbol(uri, '/');
      concepts.put(conceptId, typeUri);
    }
    return concepts;
  }

  public List<Pair<String, Long>> getOutcomeVariableUidsByStudyForSingleOutcome(String namespaceUid, List<String> studyUids, String outcomeURI) {
    String query = "TODO";

    System.out.println("getOutcomeVariableUidsByStudyForSingleOutcome query: " + query);
    String response = queryTripleStore(query);
    System.out.println("getOutcomeVariableUidsByStudyForSingleOutcome response: " + response);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    List<Pair<String, Long>> studyVariablesForOutcome = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String studyUid = findStudyIdInURI(uri);
      Long variableId = Long.valueOf(subStringAfterLastSymbol(uri, '/'));
      studyVariablesForOutcome.add(Pair.of(studyUid, variableId));
    }
    return studyVariablesForOutcome;
  }

  @Override
  public Map<String, String> getTrialverseVariables(String namespaceUid, String studyId, Collection<String> outcomeURIs) {
    String optionString = buildOptionStringFromConceptURIs(outcomeURIs);
    String query1 = "TODO";

    String response = queryTripleStore(query1);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Map<String, String> concepts = new HashMap<>(bindings.size());
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.uri.value");
      String typeUri = JsonPath.read(binding, "$.type.value");
      String conceptId = subStringAfterLastSymbol(uri, '/');
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
      String studyUid = JsonPath.read(binding, "$.studyUid.value");
      String drugUid = JsonPath.read(binding, "$.drugUid.value");
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
        return subStringAfterLastSymbol(s, '/');
      }
    });
    return StringUtils.join(strippedUris, "|");
  }

  private String buildOptionStringFromIds(List<String> ids) {
    return StringUtils.join(ids, "|");
  }

  public List<String> findStudiesReferringToConcept(String namespaceUid, String conceptUri) {
    List<String> studyUids = new ArrayList<>();
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
      String studyUid = findStudyIdInURI(uri);
      studyUids.add(studyUid);
    }

    return studyUids;
  }

  private String findStudyIdInURI(String uri) {
    // extract numerical study id
    Matcher matcher = STUDY_UID_FROM_URI_PATTERN.matcher(uri);
    matcher.find();
    return (String.valueOf(matcher.group(1)));
  }

  private String subStringAfterLastSymbol(String inStr, char symbol) {
    return inStr.substring(inStr.lastIndexOf(symbol) + 1);
  }

}
