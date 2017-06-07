package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.graph.GraphFactory;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.*;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.model.trialdata.CovariateStudyValue;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.util.JenaProperties;
import org.drugis.trialverse.util.Namespaces;
import org.hibernate.annotations.Cache;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.StringReader;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.drugis.addis.trialverse.TrialverseUtilService.readValue;
import static org.drugis.addis.trialverse.TrialverseUtilService.subStringAfterLastSymbol;


/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {

  public final static String STUDY_DATE_FORMAT = "yyyy-MM-dd";
  public final static String NAMESPACE = TriplestoreService.loadResource("sparql/namespace.sparql");
  public final static String POPCHAR_DATA_QUERY = TriplestoreService.loadResource("sparql/populationCharacteristicCovariateData.sparql");
  public final static String STUDY_QUERY = TriplestoreService.loadResource("sparql/studyQuery.sparql");
  public final static String STUDY_DETAILS_QUERY = TriplestoreService.loadResource("sparql/studyDetails.sparql");
  public final static String STUDY_GROUPS_QUERY = TriplestoreService.loadResource("sparql/studyGroups.sparql");
  public final static String STUDY_ARMS_EPOCHS = TriplestoreService.loadResource("sparql/studyEpochs.sparql");
  public final static String STUDY_TREATMENT_ACTIVITIES = TriplestoreService.loadResource("sparql/studyTreatmentActivities.sparql");
  public final static String STUDY_DATA = TriplestoreService.loadResource("sparql/studyData.sparql");
  public final static String SINGLE_STUDY_MEASUREMENTS = TriplestoreService.loadResource("sparql/singleStudyMeasurements.sparql");
  public final static String TRIAL_DATA = TriplestoreService.loadResource("sparql/trialData.sparql");
  public final static String OUTCOME_QUERY = TriplestoreService.loadResource("sparql/outcomes.sparql");
  public final static String POPCHAR_QUERY = TriplestoreService.loadResource("sparql/populationCharacteristics.sparql");
  public final static String INTERVENTION_QUERY = TriplestoreService.loadResource("sparql/interventions.sparql");
  public final static String UNIT_CONCEPTS = TriplestoreService.loadResource("sparql/unitConcepts.sparql");
  public static final String QUERY_ENDPOINT = "/query";
  public static final String QUERY_PARAM_QUERY = "query";
  public static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";
  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
  private final static Logger logger = LoggerFactory.getLogger(TriplestoreServiceImpl.class);
  private static final HttpHeaders getJsonHeaders = createGetJsonHeader();
  public static final HttpEntity<String> acceptJsonRequest = new HttpEntity<>(getJsonHeaders);
  private static final HttpHeaders getSparqlResultsHeaders = createGetSparqlResultHeader();
  public static final HttpEntity<String> acceptSparqlResultsRequest = new HttpEntity<>(getSparqlResultsHeaders);
  private static final String DATATYPE_DURATION = "http://www.w3.org/2001/XMLSchema#duration";

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private CovariateRepository covariateRepository;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private InterventionService interventionService;

  @Inject
  private QueryResultMappingService queryResultMappingService;

  private static HttpHeaders createGetJsonHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(ACCEPT_HEADER, WebConstants.getApplicationJsonUtf8Value());
    return headers;
  }

  private static HttpHeaders createGetSparqlResultHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.put(ACCEPT_HEADER, Collections.singletonList(APPLICATION_SPARQL_RESULTS_JSON));
    return headers;
  }

  @Override
  public Collection<Namespace> queryNameSpaces() throws ParseException {
    UriComponents uriComponents = UriComponentsBuilder
            .fromHttpUrl(WebConstants.getTriplestoreBaseUri())
            .path("datasets/")
            .build();

    final ResponseEntity<String> response = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, acceptJsonRequest, String.class);
    List<Namespace> namespaces = new ArrayList<>();

    JSONArray namespacesResponse = (JSONArray) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(response.getBody());

    for (Object namespaceNode : namespacesResponse) {
      JSONObject jsonNode = (JSONObject) namespaceNode;
      String namespaceUri = jsonNode.get("id").toString();
      logger.debug("query namespaces; URI: " + namespaceUri);

      namespaces.add(getNamespaceHead(new TriplestoreUuidAndOwner(namespaceUri, null)));
    }

    return namespaces;
  }

  @Override
  @Cacheable(cacheNames = "triplestoreVersionedNameSpace", key = "#datasetUri.hashCode() + #versionUri.toString()")
  public Namespace getNamespaceVersioned(TriplestoreUuidAndOwner datasetUri, URI versionUri) {
    ResponseEntity<String> response = queryTripleStoreVersion(datasetUri.getTriplestoreUuid(), NAMESPACE, versionUri);
    return buildNameSpace(datasetUri, response);
  }

  @Override
  public Namespace getNamespaceHead(TriplestoreUuidAndOwner datasetUriAndOwner) {
    ResponseEntity<String> response = queryTripleStoreHead(datasetUriAndOwner.getTriplestoreUuid(), NAMESPACE);
    return buildNameSpace(datasetUriAndOwner, response);
  }

  private Namespace buildNameSpace(TriplestoreUuidAndOwner triplestoreUuidAndOwner, ResponseEntity<String> response) {
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    JSONObject binding = (JSONObject) bindings.get(0);
    String name = JsonPath.read(binding, "$.label.value");
    URI headVersion = URI.create(getHeadVersion(WebConstants.buildDatasetUri(triplestoreUuidAndOwner.getTriplestoreUuid())));
    String description = binding.containsKey("comment") ? (String) JsonPath.read(binding, "$.comment.value") : "";
    Integer numberOfStudies = Integer.parseInt(JsonPath.read(binding, "$.numberOfStudies.value"));
    URI version = URI.create(response.getHeaders().get(X_EVENT_SOURCE_VERSION).get(0));
    return new Namespace(triplestoreUuidAndOwner.getTriplestoreUuid(), triplestoreUuidAndOwner.getOwnerId(), name, description, numberOfStudies, version, headVersion);
  }

  @Override
  public String getHeadVersion(URI datasetUri) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(WebConstants.ACCEPT_HEADER, "text/turtle,text/html");
    ResponseEntity<String> response = restTemplate.exchange(datasetUri, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
    Graph graph = GraphFactory.createDefaultGraph();
    StringReader reader = new StringReader(response.getBody());
    RDFDataMgr.read(graph, reader, "http://example.com", RDFLanguages.TURTLE);
    Model datasetModel = ModelFactory.createModelForGraph(graph);
    Resource resource = datasetModel.getResource(datasetUri.toString());
    Statement headVersion = resource.getProperty(JenaProperties.headVersionProperty);
    RDFNode headObject = headVersion.getObject();
    return headObject.toString();
  }

  @Override
  @Cacheable(cacheNames = "triplestoreOutcomes", key = "#namespaceUid+(#versionUri!=null ? #versionUri.toString():'')")
  public List<SemanticVariable> getOutcomes(String namespaceUid, URI versionUri) throws ReadValueException {
    String query = StringUtils.replace(OUTCOME_QUERY, "$namespaceUid", namespaceUid);
    return getSemanticVariables(namespaceUid, versionUri, query, "outcome");
  }

  @Override
  public List<SemanticVariable> getPopulationCharacteristics(String namespaceUid, URI versionUri) throws ReadValueException {
    String query = StringUtils.replace(POPCHAR_QUERY, "$namespaceUid", namespaceUid);
    return getSemanticVariables(namespaceUid, versionUri, query, "populationCharacteristic");
  }

  private List<SemanticVariable> getSemanticVariables(String namespaceUid, URI versionUri, String query, String variableType) throws ReadValueException {
    List<SemanticVariable> outcomes = new ArrayList<>();
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, versionUri);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    for (Object binding : bindings) {
      JSONObject row = (JSONObject) binding;
      URI uri = readValue(row, variableType);
      String label = readValue(row, "label");
      outcomes.add(new SemanticVariable(uri, label));
    }
    return outcomes;
  }

  @Override
  @Cacheable(cacheNames = "triplestoreInterventions", key = "#namespaceUid+#version.toString()")
  public List<SemanticInterventionUriAndName> getInterventions(String namespaceUid, URI version) {
    List<SemanticInterventionUriAndName> interventions = new ArrayList<>();
    JSONArray bindings = executeQuery(namespaceUid, version, INTERVENTION_QUERY);
    for (Object binding : bindings) {
      String uri = JsonPath.read(binding, "$.intervention.value");
      String label = JsonPath.read(binding, "$.label.value");
      interventions.add(new SemanticInterventionUriAndName(URI.create(uri), label));
    }
    return interventions;
  }

  @Override
  public List<URI> getUnitUris(String namespaceUuid, URI version) {
    JSONArray bindings = executeQuery(namespaceUuid, version, UNIT_CONCEPTS);
    return bindings.stream()
            .map(binding -> URI.create(JsonPath.read(binding, "$.unitConcept.value")))
            .collect(Collectors.toList());
  }

  private JSONArray executeQuery(String namespaceUid, URI version, String query1) {
    String query = StringUtils.replace(query1, "$namespaceUid", namespaceUid);
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    return JsonPath.read(response.getBody(), "$.results.bindings");
  }

  @Override
  @Cacheable(cacheNames = "triplestoreQueryStudies", key = "#namespaceUid+#version.toString()")
  public List<Study> queryStudies(String namespaceUid, URI version) {
    JSONArray bindings = executeQuery(namespaceUid, version, STUDY_QUERY);

    Map<String, Study> studyCache = new HashMap<>();
    Map<Pair<String, String>, StudyTreatmentArm> studyArmsCache = new HashMap<>();

    for (Object binding : bindings) {
      String studyUri = JsonPath.read(binding, "$.studyUri.value");
      String studyUuid = subStringAfterLastSymbol(studyUri, '/');
      String studyGraphUri = JsonPath.read(binding, "$.studyGraphUri.value");
      String studyGraphUuid = subStringAfterLastSymbol(studyGraphUri, '/');
      String name = JsonPath.read(binding, "$.label.value");
      String title = JsonPath.read(binding, "$.title.value");
      String outcomeUidStr = JsonPath.read(binding, "$.outcomeUids.value");
      String[] outcomeUids = StringUtils.split(outcomeUidStr, ", ");
      String armUid = JsonPath.read(binding, "$.armUid.value");
      String interventionUid = JsonPath.read(binding, "$.drugUid.value");

      Study study = studyCache.get(studyUuid);
      if(study == null) {
        study = new Study(studyUuid, studyGraphUuid, name, title, Arrays.asList(outcomeUids));
      }

      StudyTreatmentArm studyArm = studyArmsCache.get(Pair.of(studyUuid, armUid));
      if(studyArm == null) {
        studyArm = new StudyTreatmentArm(armUid);
      }

      studyArm.getInterventionUids().add(interventionUid);
      studyArmsCache.put(Pair.of(studyUuid, armUid), studyArm);
      study.getTreatmentArms().add(studyArm);
      studyCache.put(studyUuid, study);

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
  public JSONArray getStudyGroups(String namespaceUid, String studyUuid) {
    String query = StringUtils.replace(STUDY_GROUPS_QUERY, "$studyUuid", studyUuid);
    return getQueryResultList(namespaceUid, query);
  }

  @Override
  public JSONArray getStudyEpochs(String namespaceUid, String studyUuid) {
    String query = StringUtils.replace(STUDY_ARMS_EPOCHS, "$studyUuid", studyUuid);
    return getQueryResultList(namespaceUid, query);
  }

  private Integer tryParseInt(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Long tryParseLong(String str) {
    try {
      return Long.parseLong(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Double tryParseDouble(String str) {
    try {
      return Double.parseDouble(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public List<TreatmentActivity> getStudyTreatmentActivities(String namespaceUid, String studyUuid) {
    String query = StringUtils.replace(STUDY_TREATMENT_ACTIVITIES, "$studyUuid", studyUuid);
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
  public List<StudyData> getStudyData(String namespaceUid, String studyUuid, StudyDataSection studyDataSection) {
    String query = StringUtils.replace(STUDY_DATA, "$studyUuid", studyUuid);
    query = StringUtils.replace(query, "$studyDataType", studyDataSection.toString());
    logger.debug(query);
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

      AbstractStudyDataValue studyDataArmValue;
      String armInstanceUid = (String) jsonObject.get("instanceUid");
      String label = (String) jsonObject.get("groupLabel");
      Boolean isArm = Boolean.parseBoolean((String) jsonObject.get("isArm"));
      Integer sampleSize = jsonObject.containsKey("sampleSize") ? tryParseInt((String) jsonObject.get("sampleSize")) : null; // FIXME: why is this an integer when the count is Long?
      String sampleDuration = jsonObject.containsKey("sampleDuration") ? (String) jsonObject.get("sampleDuration") : null;

      if (jsonObject.containsKey("count")) {
        studyDataArmValue = new RateStudyDataValue.RateStudyDataValueBuilder(armInstanceUid, label, isArm)
                .count(tryParseLong((String) jsonObject.get("count")))
                .sampleSize(sampleSize)
                .sampleDuration(sampleDuration)
                .build();
        moment.getStudyDataValues().add(studyDataArmValue);
      } else if (jsonObject.containsKey("mean") || jsonObject.containsKey("std")) {
        studyDataArmValue = new ContinuousStudyDataValue.ContinuousStudyDataValueBuilder(armInstanceUid, label, isArm)
                .mean(jsonObject.containsKey("mean") ? tryParseDouble((String) jsonObject.get("mean")) : null)
                .std(jsonObject.containsKey("std") ? tryParseDouble((String) jsonObject.get("std")) : null)
                .sampleSize(sampleSize)
                .sampleDuration(sampleDuration)
                .build();
        moment.getStudyDataValues().add(studyDataArmValue);
      } else if (jsonObject.containsKey("categoryCount")) {
        CategoricalStudyDataValue existingValue = findExistingCategoricalArmValue(armInstanceUid, moment.getStudyDataValues());
        if(existingValue == null) {
          existingValue = new CategoricalStudyDataValue(armInstanceUid, label, isArm);
          moment.getStudyDataValues().add(existingValue);
        }
        Pair<String, Integer> value = Pair.of((String) jsonObject.get("categoryLabel"), Integer.parseInt((String) jsonObject.get("categoryCount")));
        existingValue.getValues().add(value);
      }

    }
    return new ArrayList<>(stringStudyDataMap.values());
  }

  private CategoricalStudyDataValue findExistingCategoricalArmValue(String armInstanceUid, List<AbstractStudyDataValue> studyDataArmValues) {
    for(AbstractStudyDataValue armValue: studyDataArmValues) {
      if (armValue.getInstanceUid().equals(armInstanceUid) && armValue instanceof CategoricalStudyDataValue) {
        return (CategoricalStudyDataValue) armValue;
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
      Object value = JsonPath.read(binding, "$. " + key + ".value");
      newObject.put(key, value);
    }
    return newObject;
  }

  private StudyWithDetails buildStudyWithDetailsFromJsonObject(Object binding) {
    JSONObject row = (net.minidev.json.JSONObject) binding;
    String graphUuid = row.containsKey("graphUri") ? subStringAfterLastSymbol(JsonPath.read(binding, "$.graphUri.value"), '/') : null;
    String uid = subStringAfterLastSymbol(JsonPath.read(binding, "$.studyUri.value"), '/');
    String name = row.containsKey("label") ? JsonPath.read(binding, "$.label.value") : null;
    String title = row.containsKey("title") ? JsonPath.read(binding, "$.title.value") : null;
    Integer studySize = row.containsKey("studySize") ? Integer.parseInt(JsonPath.read(binding, "$.studySize.value")) : null;
    String allocation = row.containsKey("allocation") ? StudyAllocationEnum.fromString(subStringAfterLastSymbol(JsonPath.read(binding, "$.allocation.value"), '#')).toString() : null;
    String blinding = row.containsKey("blinding") ? StudyBlindingEnum.fromString(subStringAfterLastSymbol(JsonPath.read(binding, "$.blinding.value"), '#')).toString() : null;
    String inclusionCriteria = row.containsKey("inclusionCriteria") ? JsonPath.read(binding, "$.inclusionCriteria.value") : null;
    Integer numberOfStudyCenters = row.containsKey("numberOfCenters") ? Integer.parseInt(JsonPath.read(binding, "$.numberOfCenters.value")) : null;
    String publicationURLs = row.containsKey("publications") ? JsonPath.read(binding, "$.publications.value") : null;
    String status = row.containsKey("status") ? StudyStatusEnum.fromString(subStringAfterLastSymbol(JsonPath.read(binding, "$.status.value"), '#')).toString() : null;
    String indication = row.containsKey("indication") ? JsonPath.read(binding, "$.indication.value") : null;
    String objective = row.containsKey("objective") ? JsonPath.read(binding, "$.objective.value") : null;
    String investigationalDrugNames = row.containsKey("drugNames") ? JsonPath.read(binding, "$.drugNames.value") : null;
    Integer numberOfArms = row.containsKey("numberOfArms") ? Integer.parseInt(JsonPath.read(binding, "$.numberOfArms.value")) : null;

    DateTimeFormatter formatter = DateTimeFormat.forPattern(STUDY_DATE_FORMAT);
    DateTime startDate = row.containsKey("startDate") ? formatter.parseDateTime(JsonPath.read(binding, "$.startDate.value")).toDateMidnight().toDateTime() : null;
    DateTime endDate = row.containsKey("endDate") ? formatter.parseDateTime(JsonPath.read(binding, "$.endDate.value")).toDateMidnight().toDateTime() : null;

    String dosing = row.containsKey("doseType") ? JsonPath.read(binding, "$.doseType.value") : "Fixed"; //todo needs better way of querying

    return new StudyWithDetails
            .StudyWithDetailsBuilder()
            .graphUuid(graphUuid)
            .studyUuid(uid)
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

  public static String buildInterventionUnionString(Set<URI> interventionUris) {
    String result = "";
    for (URI interventionUri : interventionUris) {
      result += " { ?interventionInstance owl:sameAs <" + interventionUri + "> } UNION \n";
    }

    return result.substring(0, result.lastIndexOf("UNION"));
  }

  public static String buildOutcomeUnionString(Set<URI> uris) {
    String result = "";
    for (URI outcomeUri : uris) {
      result += " { ?outcomeInstance ontology:of_variable [ owl:sameAs <" + outcomeUri + "> ] } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  @Override
  public List<TrialDataStudy> getNetworkData(String namespaceUid, URI version, URI outcomeUri,
                                             Set<URI> interventionUris, Set<String> covariateKeys) throws ReadValueException {
      return getTrialData(namespaceUid, version, "?graph", Collections.singleton(outcomeUri), interventionUris, covariateKeys);
  }

  @Override
  public List<TrialDataStudy> getSingleStudyData(String namespaceUid, URI studyUri, URI version, Set<URI> outcomeUris, Set<URI> interventionUris) throws ReadValueException {
    String graphSelector = studyUri == null ? null : "<" + studyUri.toString() + ">";
    return getTrialData(namespaceUid, version, graphSelector, outcomeUris, interventionUris, Collections.emptySet());
  }

  @Override
  public List<TrialDataStudy> getAllTrialData(String namespaceUid, URI datasetVersion, Set<URI> outcomeUris,
                                              Set<URI> interventionUris) throws ReadValueException {
    return getTrialData(namespaceUid, datasetVersion, "?graph", outcomeUris, interventionUris, Collections.emptySet());
  }

  private List<TrialDataStudy> getTrialData(String namespaceUid, URI version, String graphSelector, Set<URI> outcomeUris,
                                            Set<URI> interventionUris, Set<String> covariateKeys) throws ReadValueException {
    if (interventionUris.isEmpty() || outcomeUris.isEmpty() || graphSelector == null) {
      return Collections.emptyList();
    }
    String interventionUnion = buildInterventionUnionString(interventionUris);
    String outcomeUnion = buildOutcomeUnionString(outcomeUris);
    String query = TRIAL_DATA
            .replace("$graphSelector", graphSelector)
            .replace("$outcomeUnionString", outcomeUnion)
            .replace("$interventionUnionString", interventionUnion);

    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");

    Map<URI, TrialDataStudy> trialDataStudies = queryResultMappingService.mapResultRowsToTrialDataStudy(bindings);

    List<CovariateOption> covariateOptions = Arrays.asList(CovariateOption.values());
    // transform covariate keys to object
    List<CovariateOption> studyLevelCovariates = covariateKeys.stream()
            .filter(isStudyLevelCovariate(covariateOptions))
            .map(CovariateOption::fromKey).collect(Collectors.toList());
    List<String> populationCharacteristicCovariateKeys = covariateKeys.stream()
            .filter(isStudyLevelCovariate(covariateOptions).negate())
            .map(key -> key.startsWith(Namespaces.CONCEPT_NAMESPACE) ?
                    key.substring(Namespaces.CONCEPT_NAMESPACE.length()) :
                    key)
            .collect(Collectors.toList());

    // fetch the study-level values for each study
    List<CovariateStudyValue> covariateValues = getStudyLevelCovariateValues(namespaceUid, version, studyLevelCovariates);

    // fetch the population characteristics values for each study
    for(String popcharUuid: populationCharacteristicCovariateKeys) {
      String popcharQuery = POPCHAR_DATA_QUERY.replace("$populationCharacteristicUuid", popcharUuid);
        ResponseEntity<String> dataResponse = queryTripleStoreVersion(namespaceUid, popcharQuery, version);
      JSONArray covariateBindings = JsonPath.read(dataResponse.getBody(), "$.results.bindings");
      for (Object binding : covariateBindings) {
        CovariateStudyValue covariateStudyValue = queryResultMappingService.mapResultToCovariateStudyValue((JSONObject) binding);
        covariateValues.add(covariateStudyValue);
      }
    }

    // add the values to the studyData object
    for (CovariateStudyValue covariateStudyValue : covariateValues) {
      TrialDataStudy studyData = trialDataStudies.get(covariateStudyValue.getStudyUri());
      if (studyData != null) {
        studyData.addCovariateValue(covariateStudyValue);
      }
    }

    return new ArrayList<>(trialDataStudies.values());
  }


  @Override
  public Set<AbstractIntervention> findMatchingIncludedInterventions(Set<AbstractIntervention> includedInterventions, TrialDataArm arm) {
    return includedInterventions.stream().filter(intervention -> {
      try {
        return interventionService.isMatched(intervention, arm.getSemanticInterventions());
      } catch (InvalidTypeForDoseCheckException | ResourceDoesNotExistException e) {
        e.printStackTrace();
      }
      return false;
    }).collect(Collectors.toSet());
  }


  @Override
  public List<TrialDataStudy> addMatchingInformation(Set<AbstractIntervention> includedInterventions, List<TrialDataStudy> trialData) {
    for (TrialDataStudy study : trialData) {
      for (TrialDataArm arm : study.getTrialDataArms()) {
        Set<AbstractIntervention> matchingInterventions = findMatchingIncludedInterventions(includedInterventions, arm);
        Set<Integer> matchingInterventionIds = matchingInterventions.stream().map(AbstractIntervention::getId).collect(Collectors.toSet());
        arm.setMatchedProjectInterventionIds(matchingInterventionIds);

      }
    }
    return trialData;
  }

  private Predicate<String> isStudyLevelCovariate(List<CovariateOption> covariateOptions) {
    return key -> covariateOptions.stream().filter(option -> key.equals(option.toString())).findFirst().isPresent();
  }

  @Override
  public List<CovariateStudyValue> getStudyLevelCovariateValues(String namespaceUid, URI version,
                                                                List<CovariateOption> covariates) throws ReadValueException {
    List<CovariateStudyValue> covariateStudyValues = new ArrayList<>();
    for (CovariateOption covariate : covariates) {
      ResponseEntity<String> covariateResponse = queryTripleStoreVersion(namespaceUid, covariate.getQuery(), version);
      JSONArray covariateBindings = JsonPath.read(covariateResponse.getBody(), "$.results.bindings");
      for (Object binding : covariateBindings) {
        JSONObject row = (JSONObject) binding;
        URI studyUri = readValue(row, "graph");
        Double value = extractValueFromRow(row);
        CovariateStudyValue covariateStudyValue = new CovariateStudyValue(studyUri, covariate.toString(), value);
        covariateStudyValues.add(covariateStudyValue);
      }
    }
    return covariateStudyValues;
  }

  private Double extractValueFromRow(JSONObject row) {
    Double value = null;
    if (row.containsKey("value")) {
      String valueAsString = JsonPath.read(row, "$.value.value");
      if (JsonPath.<String>read(row, "$.value.datatype").equals(DATATYPE_DURATION)) {
        Period period = Period.parse(valueAsString);
        if(period.getMonths() > 0) {
          value = period.getMonths() * 30.0;
        } else {
          Integer periodAsDays = period.toStandardDays().getDays();
          value = periodAsDays.doubleValue();
        }
      } else {
        value = Double.parseDouble(valueAsString);
      }
    }
    return value;
  }

  private ResponseEntity<String> queryTripleStoreHead(String datasetUri, String query) {
    String datasetUuid = subStringAfterLastSymbol(datasetUri, '/');

    logger.debug("Triplestore uri = " + WebConstants.getTriplestoreBaseUri());
    logger.debug("sparql query = " + query);
    logger.debug("dataset uuid = " + datasetUuid);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(WebConstants.getTriplestoreBaseUri())
            .path("datasets/" + datasetUuid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, query)
            .build();

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, acceptSparqlResultsRequest, String.class);
  }

  @Cacheable(cacheNames="versionedDatasetQuery", key="#namespaceUid+(#versionUri != null?#versionUri.toString():'headVersion')+#query.hashCode()")
  private ResponseEntity<String> queryTripleStoreVersion(String namespaceUid, String query, URI versionUri) {
    logger.debug("Triplestore uri = " + WebConstants.getTriplestoreBaseUri());
    logger.debug("namespaceUid = " + namespaceUid);
    logger.debug("versionUri = " + versionUri);
    logger.debug("sparql query = " + query);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(WebConstants.getTriplestoreBaseUri())
            .path("datasets/" + namespaceUid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, query)
            .build();

    HttpHeaders headers = new HttpHeaders();

    headers.put(X_ACCEPT_EVENT_SOURCE_VERSION, Collections.singletonList(versionUri.toString()));
    headers.put(ACCEPT_HEADER, Collections.singletonList(APPLICATION_SPARQL_RESULTS_JSON));

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }
}
