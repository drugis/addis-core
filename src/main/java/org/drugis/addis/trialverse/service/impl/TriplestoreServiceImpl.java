package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyAllocationEnum;
import org.drugis.addis.trialverse.model.emun.StudyBlindingEmun;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.model.emun.StudyStatusEnum;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {

  public final static String STUDY_DATE_FORMAT = "yyyy-MM-dd";

  private final static Logger logger = LoggerFactory.getLogger(TriplestoreServiceImpl.class);

  private final static String LATEST_EVENT_QUERY = loadResource("sparql/latestEvent.sparql");
  private final static String NAMESPACE_QUERY = loadResource("sparql/namespaceQuery.sparql");
  private final static String NAMESPACE = loadResource("sparql/namespace.sparql");
  private final static String STUDY_QUERY = loadResource("sparql/studyQuery.sparql");
  private final static String STUDY_DETAILS_QUERY = loadResource("sparql/studyDetails.sparql");
  private final static String STUDY_ARMS_QUERY = loadResource("sparql/studyArms.sparql");
  private final static String STUDY_ARMS_EPOCHS = loadResource("sparql/studyEpochs.sparql");
  private final static String STUDY_TREATMENT_ACTIVITIES = loadResource("sparql/studyTreatmentActivities.sparql");
  private final static String STUDY_DATA = loadResource("sparql/studyData.sparql");
  private final static String SINGLE_STUDY_MEASUREMENTS = loadResource("sparql/singleStudyMeasurements.sparql");
  private final static String TRIAL_DATA = loadResource("sparql/trialData.sparql");
  private final static String OUTCOME_QUERY = loadResource("sparql/outcomes.sparql");
  private final static String INTERVENTION_QUERY = loadResource("sparql/interventions.sparql");

  @Inject
  RestOperationsFactory restOperationsFactory;

  private static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      String query = IOUtils.toString(stream, "UTF-8");
      return query;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  private String getLatestEvent() {
    String query = LATEST_EVENT_QUERY;
    String response = queryHistory(query);
    String lastEventId;

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    if(bindings.size() != 1) {
      logger.error("there can only be one latestEvent");
      throw new RuntimeException();
    }
    Object binding = bindings.get(0);
     lastEventId = JsonPath.read(binding, "$.lastEventId.value");

    return lastEventId;
  }


  @Override
  public Collection<Namespace> queryNameSpaces() {
    String query = NAMESPACE_QUERY;
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    String currentVersionUri = getLatestEvent();
    List<Namespace> namespaces = new ArrayList<>(bindings.size());
    for (Object binding : bindings) {
      String uid = JsonPath.read(binding, "$.dataset.value");
      uid = subStringAfterLastSymbol(uid, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String description = JsonPath.read(binding, "$.comment.value");
      Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
	  
      JSONObject row = (JSONObject) binding;
      String sourceUrl = row.containsKey("sourceUrl") ? JsonPath.<String>read(row, "$.sourceUrl.value") : null;
      namespaces.add(new Namespace(uid, name, description, numberOfStudies, sourceUrl, currentVersionUri));
    }
    return namespaces;
  }

  @Override
  public Namespace getNamespace(String uid) {
    String query = StringUtils.replace(NAMESPACE, "$namespaceUid", uid);

    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Object binding = bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    String description = JsonPath.read(binding, "$.comment.value");
    Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
    JSONObject row = (JSONObject) binding;
    String sourceUrl = row.containsKey("sourceUrl") ? JsonPath.<String>read(row, "$.sourceUrl.value") : null;
    String currentVersionURI = getLatestEvent();
    return new Namespace(uid, name, description, numberOfStudies, sourceUrl, currentVersionURI);
  }


  @Override
  public List<SemanticOutcome> getOutcomes(String namespaceUid, String version) {
    List<SemanticOutcome> outcomes = new ArrayList<>();

    String query = StringUtils.replace(OUTCOME_QUERY, "$namespaceUid", namespaceUid);
    //System.out.println(query);
    String response = queryTripleStore(query, version);
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
  public List<SemanticIntervention> getInterventions(String namespaceUid, String version) {
    List<SemanticIntervention> interventions = new ArrayList<>();
    String query = StringUtils.replace(INTERVENTION_QUERY, "$namespaceUid", namespaceUid);
    String response = queryTripleStore(query, version);
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
  public List<Study> queryStudies(String namespaceUid, String version) {
    String query = StringUtils.replace(STUDY_QUERY, "$namespaceUid", namespaceUid);
    String response = queryTripleStore(query, version);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");

    Map<String, Study> studyCache = new HashMap<>();
    Map<Pair<String, String>, StudyTreatmentArm> studyArmsCache = new HashMap<>();

    for (Object binding : bindings) {
      String studyUid = JsonPath.read(binding, "$.study.value");
      studyUid = subStringAfterLastSymbol(studyUid, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String title = JsonPath.read(binding, "$.title.value");
      String outcomeUidStr = JsonPath.read(binding, "$.outcomeUids.value");
      String[] outcomeUids = StringUtils.split(outcomeUidStr, ", ");
      String armUid = JsonPath.read(binding, "$.armUid.value");
      String interventionUid = JsonPath.read(binding, "$.drugUid.value");

      Study study = studyCache.get(studyUid);
      if(study == null) {
        study = new Study(studyUid, name, title, Arrays.asList(outcomeUids));
      }

      StudyTreatmentArm studyArm = studyArmsCache.get(Pair.of(studyUid, armUid));
      if(studyArm == null) {
        studyArm = new StudyTreatmentArm(armUid);
      }

      studyArm.getInterventionUids().add(interventionUid);
      studyArmsCache.put(Pair.of(studyUid, armUid), studyArm);
      study.getTreatmentArms().add(studyArm);
      studyCache.put(studyUid, study);

    }
    return new ArrayList<>(studyCache.values());
  }

  @Override
  public StudyWithDetails getStudydetails(String namespaceUid, String studyUid) throws ResourceDoesNotExistException {
    String query = StringUtils.replace(STUDY_DETAILS_QUERY, "$namespaceUid", namespaceUid);
    query = StringUtils.replace(query, "$studyUid", studyUid);
    logger.debug(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    if (bindings.size() != 1) {
      throw new ResourceDoesNotExistException();
    }

    StudyWithDetails studyWithDetails = buildStudyWithDetailsFromJsonObject(bindings.get(0));
    return studyWithDetails;
  }

  @Override
  public JSONArray getStudyArms(String namespaceUid, String studyUid) {
    String query = StringUtils.replace(STUDY_ARMS_QUERY, "$namespaceUid", namespaceUid);
    query = StringUtils.replace(query, "$studyUid", studyUid);
    return getQueryResultList(query);
  }

  @Override
  public JSONArray getStudyEpochs(String namespaceUid, String studyUid) {
    String query = StringUtils.replace(STUDY_ARMS_EPOCHS, "$namespaceUid", namespaceUid);
    query = StringUtils.replace(query, "$studyUid", studyUid);
    return getQueryResultList(query);
  }

  public Integer tryParseInt(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Long tryParseLong(String str) {
    try {
      return Long.parseLong(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public Double tryParseDouble(String str) {
    try {
      return Double.parseDouble(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public List<TreatmentActivity> getStudyTreatmentActivities(String namespaceUid, String studyUid) {
    String query = StringUtils.replace(STUDY_TREATMENT_ACTIVITIES, "$namespaceUid", namespaceUid);
    query = StringUtils.replace(query, "$studyUid", studyUid);
    JSONArray queryResult = getQueryResultList(query);

    Map<String, TreatmentActivity> treatmentActivityMap = new HashMap<>();
    for (Object object : queryResult) {
      JSONObject jsonObject = (JSONObject) object;
      String treatmentActivityUid = (String) jsonObject.get("treatmentActivityUid");
      TreatmentActivity treatmentActivity = treatmentActivityMap.get(treatmentActivityUid);
      if (treatmentActivity == null) {
        String treatmentActivityType = (String) jsonObject.get("treatmentActivityType");
        treatmentActivity = new TreatmentActivity(treatmentActivityUid, treatmentActivityType);
        treatmentActivityMap.put(treatmentActivityUid, treatmentActivity);
      }

      String epochUid = (String) jsonObject.get("epochUid");
      String armUid = (String) jsonObject.get("armUid");
      treatmentActivity.getActivityApplications().add(new ActivityApplication(epochUid, armUid));

      if (jsonObject.containsKey("drugUid")) {
        AdministeredDrug administeredDrug = buildAdministeredDrug(jsonObject);
        treatmentActivity.getAdministeredDrugs().add(administeredDrug);
      }
    }

    return new ArrayList<>(treatmentActivityMap.values());
  }

  private AdministeredDrug buildAdministeredDrug(JSONObject jsonObject) {
    String drugUid = (String) jsonObject.get("drugUid");
    String treatmentDrugLabel = (String) jsonObject.get("treatmentDrugLabel");
    AdministeredDrug administeredDrug;
    if (jsonObject.containsKey("fixedValue")) {
      administeredDrug = new FixedAdministeredDrug.FixedAdministeredDrugBuilder()
              .drugUid(drugUid)
              .drugLabel(treatmentDrugLabel)
              .fixedDosingPeriodicity((String) jsonObject.get("fixedDosingPeriodicity"))
              .fixedUnitLabel((String) jsonObject.get("fixedUnitLabel"))
              .fixedValue(Double.parseDouble((String) jsonObject.get("fixedValue")))
              .build();
    } else {
      administeredDrug = new FlexibleAdministeredDrug.FlexibleAdministeredDrugBuilder()
              .drugUid(drugUid)
              .drugLabel(treatmentDrugLabel)
              .minDosingPeriodicity((String) jsonObject.get("minDosingPeriodicity"))
              .minUnitLabel((String) jsonObject.get("minUnitLabel"))
              .minValue(Double.parseDouble((String) jsonObject.get("minValue")))
              .maxDosingPeriodicity((String) jsonObject.get("maxDosingPeriodicity"))
              .maxUnitLabel((String) jsonObject.get("maxUnitLabel"))
              .maxValue(Double.parseDouble((String) jsonObject.get("maxValue")))
              .build();
    }
    return administeredDrug;
  }

  @Override
  public List<StudyData> getStudyData(String namespaceUid, String studyUid, StudyDataSection studyDataSection) {
    String query = StringUtils.replace(STUDY_DATA, "$namespaceUid", namespaceUid);
    query = StringUtils.replace(query, "$studyUid", studyUid);
    query = StringUtils.replace(query, "$studyDataType", studyDataSection.toString());
    logger.info(query);
    JSONArray queryResult = getQueryResultList(query);

    Map<String, StudyData> stringStudyDataMap = new HashMap<>();
    Map<Pair<String, String>, StudyDataMoment> outcomeMomentCache = new HashMap<>();

    for (Object object : queryResult) {
      JSONObject jsonObject = (JSONObject) object;

      String studyDataTypeUri = (String) jsonObject.get("studyDataTypeUri");
      StudyData studyData = stringStudyDataMap.get(studyDataTypeUri);
      if (studyData == null) {
        String studyDataTypeLabel = (String) jsonObject.get("studyDataTypeLabel");
        studyData = new StudyData(studyDataSection, studyDataTypeUri, studyDataTypeLabel);
        stringStudyDataMap.put(studyDataTypeUri, studyData);
      }
      String outcomeUid = (String) jsonObject.get("outcomeUid");
      String momentUid = (String) jsonObject.get("momentUid");

      StudyDataMoment moment = outcomeMomentCache.get(Pair.of(outcomeUid, momentUid));
      if (moment == null) {
        String relativeToAnchorOntology = ((String) jsonObject.get("relativeToAnchor"));
        String timeOffsetDuration = ((String) jsonObject.get("timeOffset"));
        String relativeToEpochLabel = ((String) jsonObject.get("relativeToEpochLabel"));
        moment = new StudyDataMoment(relativeToAnchorOntology, timeOffsetDuration, relativeToEpochLabel);
        outcomeMomentCache.put(Pair.of(outcomeUid, momentUid), moment);
        studyData.getStudyDataMoments().add(moment);
      }

      AbstractStudyDataArmValue studyDataArmValue;
      String armInstanceUid = (String) jsonObject.get("armInstanceUid");
      String armLabel = (String) jsonObject.get("armLabel");
      Integer sampleSize = jsonObject.containsKey("sampleSize") ? tryParseInt((String) jsonObject.get("sampleSize")) : null; // FIXME: why is this an integer when the count is Long?
      String sampleDuration = jsonObject.containsKey("sampleDuration") ? (String) jsonObject.get("sampleDuration") : null;

      if (jsonObject.containsKey("count")) {
        studyDataArmValue = new RateStudyDataArmValue
                .RateStudyDataArmValueBuilder(armInstanceUid, armLabel)
                .count(tryParseLong((String) jsonObject.get("count")))
                .sampleSize(sampleSize)
                .sampleDuration(sampleDuration)
                .build();
        moment.getStudyDataArmValues().add(studyDataArmValue);
      } else if (jsonObject.containsKey("mean")) {
        studyDataArmValue = new ContinuousStudyDataArmValue
                .ContinuousStudyDataArmValueBuilder(armInstanceUid, armLabel)
                .mean(jsonObject.containsKey("mean") ? tryParseDouble((String) jsonObject.get("mean")) : null)
                .std(jsonObject.containsKey("std") ? tryParseDouble((String) jsonObject.get("std")) : null)
                .sampleSize(sampleSize)
                .sampleDuration(sampleDuration)
                .build();
        moment.getStudyDataArmValues().add(studyDataArmValue);
      } else if (jsonObject.containsKey("categoryCount")) {
        CategoricalStudyDataArmValue existingValue = findExistingCategoricalArmValue(armInstanceUid, moment.getStudyDataArmValues());
        if(existingValue == null) {
          existingValue = new CategoricalStudyDataArmValue(armInstanceUid, armLabel);
          moment.getStudyDataArmValues().add(existingValue);
        }
        Pair<String, Integer> value = Pair.of((String) jsonObject.get("categoryLabel"), Integer.parseInt((String) jsonObject.get("categoryCount")));
        existingValue.getValues().add(value);
      }

    }
    return new ArrayList<>(stringStudyDataMap.values());
  }

  private CategoricalStudyDataArmValue findExistingCategoricalArmValue(String armInstanceUid, List<AbstractStudyDataArmValue> studyDataArmValues) {
    for(AbstractStudyDataArmValue armValue: studyDataArmValues) {
      if (armValue.getArmInstanceUid().equals(armInstanceUid) && armValue instanceof CategoricalStudyDataArmValue) {
        return (CategoricalStudyDataArmValue) armValue;
      }
    }
    return null;
  }


  private JSONArray getQueryResultList(String query) {
    logger.debug(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    return parseBindings(bindings);
  }

  private JSONArray parseBindings(JSONArray bindings) {
    JSONArray result = new JSONArray();
    for (Object binding : bindings) {
      JSONObject jsonObject = parseBinding(binding);
      result.add(jsonObject);
    }
    return result;
  }

  private JSONObject parseBinding(Object binding) {
    JSONObject jsonObject = (JSONObject) binding;
    JSONObject newObject = new JSONObject();
    for (String key : jsonObject.keySet()) {
      String value = JsonPath.read(binding, "$. " + key + ".value");
      newObject.put(key, value);
    }
    return newObject;
  }

  @Override
  public List<StudyWithDetails> queryStudydetails(String namespaceUid) {
    List<StudyWithDetails> studiesWithDetail = new ArrayList<>();
    String query = "PREFIX ontology: <http://trials.drugis.org/ontology#>\n" +
            "PREFIX dataset: <http://trials.drugis.org/datasets/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
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
            "               ontology:has_activity_application [\n" +
            "                 ontology:applied_to_arm ?arm \n" +
            "               ] ;\n" +
            "               ontology:has_drug_treatment [ a ontology:TitratedDoseDrugTreatment ] .\n" +
            "               ?study ontology:has_arm ?arm .\n" +
            "            } GROUP BY ?study ?doseType\n" +
            "              HAVING (COUNT(*) > 0)\n" +
            "      }\n" +
            "    OPTIONAL\n" +
            "    {\n" +
            "      SELECT ?study (group_concat(?drugName; separator = \", \") as ?drugNames)\n" +
            "      WHERE {\n" +
            "        GRAPH ?dataset {\n" +
            "          ?drug a ontology:Drug .\n" +
            "          ?dataset ontology:contains_study ?study \n" +
            "        }\n" +
            "        GRAPH ?study {\n" +
            "          ?instance owl:sameAs ?drug .\n" +
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
    //System.out.println(query);
    String response = queryTripleStore(query);
    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    for (Object binding : bindings) {
      studiesWithDetail.add(buildStudyWithDetailsFromJsonObject(binding));
    }
    return studiesWithDetail;
  }

  private StudyWithDetails buildStudyWithDetailsFromJsonObject(Object binding) {
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

    return new StudyWithDetails
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
  }

  private String buildInterventionUnionString(List<String> interventionUids) {
    String result = "";
    for (String interventionUid : interventionUids) {
      result = result + " { ?interventionInstance owl:sameAs entity:" + interventionUid + " } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  private String buildOutcomeUnionString(List<String> outcomeUids) {
    String result = "";
    for (String outcomeUid : outcomeUids) {
      result = result + " { ?outcomeInstance ontology:of_variable [ owl:sameAs entity:" + outcomeUid + " ] } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  @Override
  public List<TrialDataStudy> getTrialData(String namespaceUid, String version, String outcomeUid, List<String> interventionUids) {
    String interventionUnion = buildInterventionUnionString(interventionUids);
    String query = TRIAL_DATA
            .replace("$outcomeUid", outcomeUid)
            .replace("$interventionUnion", interventionUnion);

    String response = queryTripleStore(query, version);

    JSONArray bindings = JsonPath.read(response, "$.results.bindings");
    Map<String, TrialDataStudy> trialDataStudies = new HashMap<>();
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
  public List<SingleStudyBenefitRiskMeasurementRow> getSingleStudyMeasurements(String studyUid, String version, List<String> outcomeUids, List<String> interventionUids) {

    String query = StringUtils.replace(SINGLE_STUDY_MEASUREMENTS, "$studyUid", studyUid);
    query = StringUtils.replace(query, "$outcomeUnionString",  buildOutcomeUnionString(outcomeUids));
    query = StringUtils.replace(query, "$interventionUnionString", buildInterventionUnionString(interventionUids));
    logger.info(query);

    String response = queryTripleStore(query, version);
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

  private String queryTripleStore(String query) {
    logger.info("Triplestore uri = " + TRIPLESTORE_URI);
    logger.info("sparql query = " + query);
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");
    RestOperations restOperations =  restOperationsFactory.build();
    return restOperations.getForObject(TRIPLESTORE_URI + "?query={query}&output={output}", String.class, vars);
  }

  private String queryTripleStore(String query, String version) {
    logger.info("Triplestore uri = " + TRIPLESTORE_URI);
    logger.info("sparql query = " + query);
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");
    RestOperations restOperations =  restOperationsFactory.build();
    return restOperations.getForObject(TRIPLESTORE_URI + "?query={query}&output={output}&event=" + version, String.class, vars);
  }

  private String queryHistory(String query) {
    logger.info("Triplestore uri = " + HISTORY_URI);
    logger.info("sparql query = " + query);
    Map<String, String> vars = new HashMap<>();
    vars.put("query", query);
    vars.put("output", "json");
    RestOperations restOperations =  restOperationsFactory.build();
    return restOperations.getForObject(HISTORY_URI + "?query={query}&output={output}", String.class, vars);
  }

  private String subStringAfterLastSymbol(String inStr, char symbol) {
    return inStr.substring(inStr.lastIndexOf(symbol) + 1);
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

}
