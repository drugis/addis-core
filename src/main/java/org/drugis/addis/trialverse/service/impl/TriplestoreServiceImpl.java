package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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
import org.drugis.addis.util.WebConstants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
  private static final HttpHeaders getJsonHeaders = createGetJsonHeader();
  private static final HttpHeaders getSparqlResultsHeaders = createGetSparqlResultHeader();

  public final static String NAMESPACE = loadResource("sparql/namespace.sparql");
  public final static String STUDY_QUERY = loadResource("sparql/studyQuery.sparql");
  public final static String STUDY_DETAILS_QUERY = loadResource("sparql/studyDetails.sparql");
  public final static String STUDIES_WITH_DETAILS_QUERY = loadResource("sparql/studiesWithDetails.sparql");
  public final static String STUDY_ARMS_QUERY = loadResource("sparql/studyArms.sparql");
  public final static String STUDY_ARMS_EPOCHS = loadResource("sparql/studyEpochs.sparql");
  public final static String STUDY_TREATMENT_ACTIVITIES = loadResource("sparql/studyTreatmentActivities.sparql");
  public final static String STUDY_DATA = loadResource("sparql/studyData.sparql");
  public final static String SINGLE_STUDY_MEASUREMENTS = loadResource("sparql/singleStudyMeasurements.sparql");
  public final static String TRIAL_DATA = loadResource("sparql/trialData.sparql");
  public final static String OUTCOME_QUERY = loadResource("sparql/outcomes.sparql");
  public final static String INTERVENTION_QUERY = loadResource("sparql/interventions.sparql");

  public static final String QUERY_ENDPOINT = "/query";
  public static final String QUERY_PARAM_QUERY = "query";
  public static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";
  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
  public static final HttpEntity<String> acceptJsonRequest = new HttpEntity<>(getJsonHeaders);
  public static final HttpEntity<String> acceptSpaqlResultsRequest = new HttpEntity<>(getSparqlResultsHeaders);

  @Inject
  RestOperationsFactory restOperationsFactory;

  private static HttpHeaders createGetJsonHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(ACCEPT_HEADER, WebConstants.APPLICATION_JSON_UTF8_VALUE);
    return headers;
  }

  private static HttpHeaders createGetSparqlResultHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.put(ACCEPT_HEADER, Collections.singletonList(APPLICATION_SPARQL_RESULTS_JSON));
    return headers;
  }

  private static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }


  @Override
  public Collection<Namespace> queryNameSpaces() throws ParseException {
    UriComponents uriComponents = UriComponentsBuilder
            .fromHttpUrl(TRIPLESTORE_BASE_URI)
            .path("datasets/")
            .build();

    final ResponseEntity<String> response = restOperationsFactory
            .build()
            .exchange(uriComponents.toUri(), HttpMethod.GET, acceptJsonRequest, String.class);
    List<Namespace> namespaces = new ArrayList<>();

    JSONArray namespaceUris = (JSONArray) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(response.getBody());

    for (Object datasetUri : namespaceUris) {
      logger.debug("query namespaces; URI: " + datasetUri.toString());
      namespaces.add(getNamespaceHead(datasetUri.toString()));
    }

    return namespaces;
  }

  @Override
  public Namespace getNamespaceVersioned(String datasetUri, String versionUri) {
    ResponseEntity<String> response = queryTripleStoreVersion(datasetUri, NAMESPACE, versionUri);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    Object binding = bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    String description = JsonPath.read(binding, "$.comment.value");
    Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
    String version = response.getHeaders().get(X_EVENT_SOURCE_VERSION).get(0);
    return new Namespace(subStringAfterLastSymbol(datasetUri, '/'), name, description, numberOfStudies, version);
  }

  @Override
  public Namespace getNamespaceHead(String datasetUri) {
    ResponseEntity<String> response = queryTripleStoreHead(datasetUri, NAMESPACE);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    JSONObject binding = (JSONObject) bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    String description = binding.containsKey("comment") ? (String) JsonPath.read(binding, "$.comment.value") : "";
    Integer numberOfStudies = Integer.parseInt(JsonPath.<String>read(binding, "$.numberOfStudies.value"));
    String version = response.getHeaders().get(X_EVENT_SOURCE_VERSION).get(0);
    return new Namespace(subStringAfterLastSymbol(datasetUri, '/'), name, description, numberOfStudies, version);
  }


  @Override
  public List<SemanticOutcome> getOutcomes(String namespaceUid, String versionUri) {
    List<SemanticOutcome> outcomes = new ArrayList<>();

    String query = StringUtils.replace(OUTCOME_QUERY, "$namespaceUid", namespaceUid);

    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, versionUri);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
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
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
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
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");

    Map<String, Study> studyCache = new HashMap<>();
    Map<Pair<String, String>, StudyTreatmentArm> studyArmsCache = new HashMap<>();

    for (Object binding : bindings) {
      String studyUri = JsonPath.read(binding, "$.studyUri.value");
      String studyUid = subStringAfterLastSymbol(studyUri, '/');
      String studyGraphUri = JsonPath.read(binding, "$.studyGraphUri.value");
      String studyGraphUuid = subStringAfterLastSymbol(studyGraphUri, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String title = JsonPath.read(binding, "$.title.value");
      String outcomeUidStr = JsonPath.read(binding, "$.outcomeUids.value");
      String[] outcomeUids = StringUtils.split(outcomeUidStr, ", ");
      String armUid = JsonPath.read(binding, "$.armUid.value");
      String interventionUid = JsonPath.read(binding, "$.drugUid.value");

      Study study = studyCache.get(studyUid);
      if(study == null) {
        study = new Study(studyUid, studyGraphUuid, name, title, Arrays.asList(outcomeUids));
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
  public StudyWithDetails getStudydetails(String namespaceUid, String studyGraphUid) throws ResourceDoesNotExistException {
    String query = StringUtils.replace(STUDY_DETAILS_QUERY,  "$studyGraphUid", studyGraphUid);
    logger.debug(query);
    ResponseEntity<String> response = queryTripleStoreHead(namespaceUid, query);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    if (bindings.size() != 1) {
      throw new ResourceDoesNotExistException();
    }

    StudyWithDetails studyWithDetails = buildStudyWithDetailsFromJsonObject(bindings.get(0));
    studyWithDetails.setGraphUuid(studyGraphUid);
    return studyWithDetails;
  }

  @Override
  public JSONArray getStudyArms(String namespaceUid, String studyUid) {
    String query = StringUtils.replace(STUDY_ARMS_QUERY, "$studyUid", studyUid);
    return getQueryResultList(namespaceUid, query);
  }

  @Override
  public JSONArray getStudyEpochs(String namespaceUid, String studyUid) {
    String query = StringUtils.replace(STUDY_ARMS_EPOCHS, "$studyUid", studyUid);
    return getQueryResultList(namespaceUid, query);
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
    String query = StringUtils.replace(STUDY_TREATMENT_ACTIVITIES, "$studyUid", studyUid);
    JSONArray queryResult = getQueryResultList(namespaceUid, query);

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
    JSONArray queryResult = getQueryResultList(namespaceUid, query);

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


  private JSONArray getQueryResultList(String namespaceUid, String query) {
    logger.debug(query);
    ResponseEntity<String> response = queryTripleStoreHead(namespaceUid, query);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
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
  public List<StudyWithDetails> queryStudydetailsHead(String namespaceUid) {
    List<StudyWithDetails> studiesWithDetail = new ArrayList<>();
    String query = STUDIES_WITH_DETAILS_QUERY.replace("$namespaceUid", namespaceUid);
    ResponseEntity<String> response = queryTripleStoreHead(namespaceUid, query);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    for (Object binding : bindings) {
      studiesWithDetail.add(buildStudyWithDetailsFromJsonObject(binding));
    }
    return studiesWithDetail;
  }

  private StudyWithDetails buildStudyWithDetailsFromJsonObject(Object binding) {
    JSONObject row = (net.minidev.json.JSONObject) binding;
    String graphUuid = row.containsKey("graphUri") ? subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.graphUri.value"), '/') : null;
    String uid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.studyUri.value"), '/');
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
            .graphUuid(graphUuid)
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
    for (String interventionUid : interventionUids) { // FIXME: pick one
      result = result + " { ?interventionInstance owl:sameAs entity:" + interventionUid + " } UNION \n"
                      + " { ?interventionInstance owl:sameAs concept:" + interventionUid + " } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  private String buildOutcomeUnionString(List<String> outcomeUids) {
    String result = "";
    for (String outcomeUid : outcomeUids) {
      result = result + " { ?outcomeInstance ontology:of_variable [ owl:sameAs entity:" + outcomeUid + " ] } UNION \n"
                      + " { ?outcomeInstance ontology:of_variable [ owl:sameAs concept:" + outcomeUid + " ] } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  @Override
  public List<TrialDataStudy> getTrialData(String namespaceUid, String version, String outcomeUid, List<String> interventionUids) {
    String interventionUnion = buildInterventionUnionString(interventionUids);
    String query = TRIAL_DATA
            .replace("$outcomeUid", outcomeUid)
            .replace("$interventionUnion", interventionUnion);

    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    Map<String, TrialDataStudy> trialDataStudies = new HashMap<>();
    for (Object binding : bindings) {
      String studyUid = subStringAfterLastSymbol(JsonPath.<String>read(binding, "$.graph.value"), '/');
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
  public List<SingleStudyBenefitRiskMeasurementRow> getSingleStudyMeasurements(String namespaceUid, String studyUid, String version, List<String> outcomeUids, List<String> interventionUids) {

    String query = StringUtils.replace(SINGLE_STUDY_MEASUREMENTS, "$studyUid", studyUid);
    query = StringUtils.replace(query, "$outcomeUnionString", buildOutcomeUnionString(outcomeUids));
    String interventionUn = buildInterventionUnionString(interventionUids);
    query = StringUtils.replace(query, "$interventionUnionString", interventionUn);
    logger.info(query);

    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
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

  private ResponseEntity queryTripleStoreHead(String datasetUri, String query) {
    String datasetUuid = subStringAfterLastSymbol(datasetUri, '/');

    logger.info("Triplestore uri = " + TRIPLESTORE_BASE_URI);
    logger.info("sparql query = " + query);
    logger.info("dataset uuid = " + datasetUuid);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(TRIPLESTORE_BASE_URI)
            .path("datasets/" + datasetUuid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, query)
            .build();

    RestOperations restOperations =  restOperationsFactory.build(); //todo use application bean
    return restOperations.exchange(uriComponents.toUri(), HttpMethod.GET, acceptSpaqlResultsRequest, String.class);
  }


  private ResponseEntity queryTripleStoreVersion(String namespaceUid, String query, String versionUri) {
    logger.info("Triplestore uri = " + TRIPLESTORE_BASE_URI);
    logger.info("sparql query = " + query);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(TRIPLESTORE_BASE_URI)
            .path("datasets/" + namespaceUid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, query)
            .build();

    HttpHeaders headers = new HttpHeaders();

    headers.put(X_ACCEPT_EVENT_SOURCE_VERSION, Collections.singletonList(versionUri));
    headers.put(ACCEPT_HEADER, Collections.singletonList(APPLICATION_SPARQL_RESULTS_JSON));

    RestOperations restOperations =  restOperationsFactory.build(); //todo use application bean
    return restOperations.exchange(uriComponents.toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
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

    @SuppressWarnings("SimplifiableIfStatement")
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
      return sampleSize.equals(that.sampleSize) && !(stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null);

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
