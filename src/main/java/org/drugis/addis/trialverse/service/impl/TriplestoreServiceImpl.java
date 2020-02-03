package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
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
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.model.trialdata.CovariateStudyValue;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.util.JenaProperties;
import org.drugis.trialverse.util.Namespaces;
import org.joda.time.Period;
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
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.drugis.addis.trialverse.TrialverseUtilService.readValue;
import static org.drugis.addis.trialverse.TrialverseUtilService.subStringAfterLastSymbol;


/**
 * Created by connor on 2/28/14.
 */
@Service
public class TriplestoreServiceImpl implements TriplestoreService {

  public final static String NAMESPACE = TriplestoreService.loadResource("sparql/namespace.sparql");
  private final static String POPCHAR_DATA_QUERY = TriplestoreService.loadResource("sparql/populationCharacteristicCovariateData.sparql");
  private final static String STUDY_QUERY = TriplestoreService.loadResource("sparql/studyQuery.sparql");
  public final static String TRIAL_DATA = TriplestoreService.loadResource("sparql/trialData.sparql");
  private final static String OUTCOME_QUERY = TriplestoreService.loadResource("sparql/outcomes.sparql");
  private final static String POPCHAR_QUERY = TriplestoreService.loadResource("sparql/populationCharacteristics.sparql");
  private final static String INTERVENTION_QUERY = TriplestoreService.loadResource("sparql/interventions.sparql");
  private final static String UNIT_CONCEPTS = TriplestoreService.loadResource("sparql/unitConcepts.sparql");
  private final static String STUDY_TITLES = TriplestoreService.loadResource("sparql/getStudyTitles.sparql");

  public static final String QUERY_ENDPOINT = "/query";
  public static final String QUERY_PARAM_QUERY = "query";
  private static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";
  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";
  private static final String ACCEPT_HEADER = "Accept";
  private static final String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
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

  @Inject
  private WebConstants webConstants;

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
    UriComponents namespacesQueryUriComponents = UriComponentsBuilder
            .fromHttpUrl(webConstants.getTriplestoreBaseUri())
            .path("datasets/")
            .build();

    final ResponseEntity<String> response = restTemplate.exchange(namespacesQueryUriComponents.toUri(),
            HttpMethod.GET, acceptJsonRequest, String.class);
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
  public Namespace getNamespaceVersioned(TriplestoreUuidAndOwner datasetUri, URI versionUri) throws IOException {
    ResponseEntity<String> response = queryTripleStoreVersion(datasetUri.getTriplestoreUuid(), NAMESPACE, versionUri);
    return buildNameSpace(datasetUri, response);
  }

  @Override
  public Namespace getNamespaceHead(TriplestoreUuidAndOwner datasetUriAndOwner) {
    ResponseEntity<String> response = queryTripleStoreHead(datasetUriAndOwner.getTriplestoreUuid());
    return buildNameSpace(datasetUriAndOwner, response);
  }

  private Namespace buildNameSpace(TriplestoreUuidAndOwner triplestoreUuidAndOwner, ResponseEntity<String> response) {
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    JSONObject binding = new JSONObject((LinkedHashMap) bindings.get(0));
    String name = JsonPath.read(binding, "$.label.value");
    URI headVersion = URI.create(getHeadVersion(webConstants.buildDatasetUri(triplestoreUuidAndOwner.getTriplestoreUuid())));
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
    Selector selector = new SimpleSelector(null, JenaProperties.headVersionProperty, (Object) null);
    StmtIterator stmtIterator = datasetModel.listStatements(selector);
    Statement statement = stmtIterator.nextStatement();
    return statement.getObject().toString();
  }

  @Override
  @Cacheable(cacheNames = "triplestoreOutcomes", key = "#namespaceUid+(#versionUri!=null ? #versionUri.toString():'')")
  public List<SemanticVariable> getOutcomes(String namespaceUid, URI versionUri) throws ReadValueException, IOException {
    String query = StringUtils.replace(OUTCOME_QUERY, "$namespaceUid", namespaceUid);
    return getSemanticVariables(namespaceUid, versionUri, query, "outcome");
  }

  @Override
  public List<SemanticVariable> getPopulationCharacteristics(String namespaceUid, URI versionUri) throws ReadValueException, IOException {
    return getSemanticVariables(namespaceUid, versionUri, POPCHAR_QUERY, "populationCharacteristic");
  }

  private List<SemanticVariable> getSemanticVariables(String namespaceUid, URI versionUri, String query, String variableType) throws ReadValueException, IOException {
    List<SemanticVariable> outcomes = new ArrayList<>();
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, versionUri);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    for (Object binding : bindings) {
      JSONObject row = new JSONObject((LinkedHashMap) binding);
      URI uri = readValue(row, variableType);
      String label = readValue(row, "label");
      outcomes.add(new SemanticVariable(uri, label));
    }
    return outcomes;
  }

  @Override
  @Cacheable(cacheNames = "triplestoreInterventions", key = "#namespaceUid+#version.toString()")
  public List<SemanticInterventionUriAndName> getInterventions(String namespaceUid, URI version) throws IOException {
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
  public List<URI> getUnitUris(String namespaceUuid, URI version) throws IOException {
    JSONArray bindings = executeQuery(namespaceUuid, version, UNIT_CONCEPTS);
    return bindings.stream()
            .map(binding -> URI.create(JsonPath.read(binding, "$.unitConcept.value")))
            .collect(Collectors.toList());
  }

  private JSONArray executeQuery(String namespaceUid, URI version, String query1) throws IOException {
    String query = StringUtils.replace(query1, "$namespaceUid", namespaceUid);
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    return JsonPath.read(response.getBody(), "$.results.bindings");
  }

  @Override
  @Cacheable(cacheNames = "triplestoreQueryStudies", key = "#namespaceUid+#version.toString()")
  public List<Study> queryStudies(String namespaceUid, URI version) throws IOException {
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
      if (study == null) {
        study = new Study(studyUuid, studyGraphUuid, name, title, Arrays.asList(outcomeUids));
      }

      StudyTreatmentArm studyArm = studyArmsCache.get(Pair.of(studyUuid, armUid));
      if (studyArm == null) {
        studyArm = new StudyTreatmentArm(armUid);
      }

      studyArm.getInterventionUids().add(interventionUid);
      studyArmsCache.put(Pair.of(studyUuid, armUid), studyArm);
      study.getTreatmentArms().add(studyArm);
      studyCache.put(studyUuid, study);

    }
    return new ArrayList<>(studyCache.values());
  }

  public static String buildInterventionUnionString(Set<URI> interventionUris) {
    StringBuilder result = new StringBuilder();
    for (URI interventionUri : interventionUris) {
      result.append(" { ?interventionInstance owl:sameAs <").append(interventionUri).append("> } UNION \n");
    }

    return result.substring(0, result.lastIndexOf("UNION"));
  }

  public static String buildOutcomeUnionString(Set<URI> uris) {
    StringBuilder result = new StringBuilder();
    for (URI outcomeUri : uris) {
      result.append(" { ?outcomeInstance ontology:of_variable [ owl:sameAs <").append(outcomeUri).append("> ] } UNION \n");
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }

  @Override
  public List<TrialDataStudy> getNetworkData(
          String namespaceUid,
          URI version,
          URI outcomeUri,
          Set<URI> interventionUris,
          Set<String> covariateKeys
  ) throws ReadValueException, IOException {
    return getTrialData(namespaceUid, version, "?graph", Collections.singleton(outcomeUri), interventionUris, covariateKeys);
  }

  @Override
  public List<TrialDataStudy> getSingleStudyData(
          String namespaceUid,
          URI studyUri,
          URI version,
          Set<URI> outcomeUris,
          Set<URI> interventionUris
  ) throws ReadValueException, IOException {
    String graphSelector = studyUri == null ? null : "<" + studyUri.toString() + ">";
    return getTrialData(namespaceUid, version, graphSelector, outcomeUris, interventionUris, Collections.emptySet());
  }

  @Override
  public List<TrialDataStudy> getAllTrialData(String namespaceUid, URI datasetVersion, Set<URI> outcomeUris,
                                              Set<URI> interventionUris) throws ReadValueException, IOException {
    return getTrialData(namespaceUid, datasetVersion, "?graph", outcomeUris, interventionUris, Collections.emptySet());
  }

  private List<TrialDataStudy> getTrialData(
          String namespaceUid,
          URI version,
          String graphSelector,
          Set<URI> outcomeUris,
          Set<URI> interventionUris,
          Set<String> covariateKeys
  ) throws ReadValueException, IOException {
    if (interventionUris.isEmpty() || outcomeUris.isEmpty() || graphSelector == null) {
      return Collections.emptyList();
    }
    Map<URI, TrialDataStudy> trialDataStudies = getTrialDataStudies(namespaceUid, version, graphSelector, outcomeUris, interventionUris);
    addCovariateValues(namespaceUid, version, covariateKeys, trialDataStudies);
    return new ArrayList<>(trialDataStudies.values());
  }

  private void addCovariateValues(String namespaceUid, URI version, Set<String> covariateKeys, Map<URI, TrialDataStudy> trialDataStudies) throws ReadValueException, IOException {
    List<CovariateOption> covariateOptions = Arrays.asList(CovariateOption.values());
    List<CovariateOption> studyLevelCovariates = getStudyLevelCovariateKeys(covariateKeys, covariateOptions);
    List<CovariateStudyValue> covariateValues = getStudyLevelCovariateValues(namespaceUid, version, studyLevelCovariates);

    List<String> populationCharacteristicCovariateKeys = getPopulationCharacteristicCovariateKeys(covariateKeys, covariateOptions);
    getPopulationCharacteristicsValuesForStudies(namespaceUid, version, covariateValues, populationCharacteristicCovariateKeys);

    // add the values to the studyData object
    for (CovariateStudyValue covariateStudyValue : covariateValues) {
      TrialDataStudy studyData = trialDataStudies.get(covariateStudyValue.getStudyUri());
      if (studyData != null) {
        studyData.addCovariateValue(covariateStudyValue);
      }
    }
  }

  private void getPopulationCharacteristicsValuesForStudies(String namespaceUid, URI version, List<CovariateStudyValue> covariateValues, List<String> populationCharacteristicCovariateKeys) throws IOException, ReadValueException {
    for (String popcharUuid : populationCharacteristicCovariateKeys) {
      String popcharQuery = POPCHAR_DATA_QUERY.replace("$populationCharacteristicUuid", popcharUuid);
      ResponseEntity<String> dataResponse = queryTripleStoreVersion(namespaceUid, popcharQuery, version);
      JSONArray covariateBindings = JsonPath.read(dataResponse.getBody(), "$.results.bindings");
      for (Object binding : covariateBindings) {
        JSONObject jsonObject = new JSONObject((LinkedHashMap) binding);
        CovariateStudyValue covariateStudyValue = queryResultMappingService.mapResultToCovariateStudyValue(jsonObject);
        covariateValues.add(covariateStudyValue);
      }
    }
  }

  private List<String> getPopulationCharacteristicCovariateKeys(Set<String> covariateKeys, List<CovariateOption> covariateOptions) {
    return covariateKeys.stream()
            .filter(isStudyLevelCovariate(covariateOptions).negate())
            .map(key -> key.startsWith(Namespaces.CONCEPT_NAMESPACE) ?
                    key.substring(Namespaces.CONCEPT_NAMESPACE.length()) :
                    key)
            .collect(Collectors.toList());
  }

  private List<CovariateOption> getStudyLevelCovariateKeys(Set<String> covariateKeys, List<CovariateOption> covariateOptions) {
    return covariateKeys.stream()
            .filter(isStudyLevelCovariate(covariateOptions))
            .map(CovariateOption::fromKey).collect(Collectors.toList());
  }

  private Map<URI, TrialDataStudy> getTrialDataStudies(String namespaceUid, URI version, String graphSelector, Set<URI> outcomeUris, Set<URI> interventionUris) throws IOException, ReadValueException {
    String query = createTrialDataQuery(graphSelector, outcomeUris, interventionUris);
    ResponseEntity<String> response = queryTripleStoreVersion(namespaceUid, query, version);
    JSONArray bindings = JsonPath.read(response.getBody(), "$.results.bindings");
    return queryResultMappingService.mapResultRowsToTrialDataStudy(bindings);
  }

  private String createTrialDataQuery(String graphSelector, Set<URI> outcomeUris, Set<URI> interventionUris) {
    String interventionUnion = buildInterventionUnionString(interventionUris);
    String outcomeUnion = buildOutcomeUnionString(outcomeUris);
    return TRIAL_DATA
            .replace("$graphSelector", graphSelector)
            .replace("$outcomeUnionString", outcomeUnion)
            .replace("$interventionUnionString", interventionUnion);
  }

  @Override
  public List<TrialDataStudy> addMatchingInformation(Set<AbstractIntervention> includedInterventions, List<TrialDataStudy> studies) {
    for (TrialDataStudy study : studies) {
      study.getArms().forEach(arm -> {
        Set<AbstractIntervention> matchingInterventions = findMatchingIncludedInterventions(includedInterventions, arm);
        Set<Integer> matchingInterventionIds = matchingInterventions.stream()
                .map(AbstractIntervention::getId).collect(Collectors.toSet());
        arm.setMatchedProjectInterventionIds(matchingInterventionIds);
      });
    }
    return studies;
  }

  @Override
  public Set<AbstractIntervention> findMatchingIncludedInterventions(Set<AbstractIntervention> includedInterventions, TrialDataArm arm) {
    return includedInterventions.stream().filter(intervention -> {
      try {
        return interventionService.isMatched(intervention, arm.getSemanticInterventions());
      } catch (InvalidTypeForDoseCheckException | ResourceDoesNotExistException e) {
        e.printStackTrace();
        return false;
      }
    }).collect(Collectors.toSet());
  }

  private Predicate<String> isStudyLevelCovariate(List<CovariateOption> covariateOptions) {
    return key -> covariateOptions.stream().anyMatch(option -> key.equals(option.toString()));
  }

  @Override
  public List<CovariateStudyValue> getStudyLevelCovariateValues(String namespaceUid, URI version,
                                                                List<CovariateOption> covariates) throws ReadValueException, IOException {
    List<CovariateStudyValue> covariateStudyValues = new ArrayList<>();
    for (CovariateOption covariate : covariates) {
      ResponseEntity<String> covariateResponse = queryTripleStoreVersion(namespaceUid, covariate.getQuery(), version);
      JSONArray covariateBindings = JsonPath.read(covariateResponse.getBody(), "$.results.bindings");
      for (Object binding : covariateBindings) {
        JSONObject row = new JSONObject((LinkedHashMap) binding);
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
        if (period.getMonths() > 0) {
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

  private ResponseEntity<String> queryTripleStoreHead(String datasetUri) {
    String datasetUuid = subStringAfterLastSymbol(datasetUri, '/');

    logger.debug("Triplestore uri = " + webConstants.getTriplestoreBaseUri());
    logger.debug("sparql query = " + TriplestoreServiceImpl.NAMESPACE);
    logger.debug("dataset uuid = " + datasetUuid);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(webConstants.getTriplestoreBaseUri())
            .path("datasets/" + datasetUuid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, TriplestoreServiceImpl.NAMESPACE)
            .build();

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, acceptSparqlResultsRequest, String.class);
  }

  @Cacheable(cacheNames = "versionedDatasetQuery", key = "#namespaceUid+(#versionUri != null?#versionUri.toString():'headVersion')+#query.hashCode()")
  private ResponseEntity<String> queryTripleStoreVersion(String namespaceUid, String query, URI versionUri) throws IOException {
    logger.debug("Triplestore uri = " + webConstants.getTriplestoreBaseUri());
    logger.debug("namespaceUid = " + namespaceUid);
    logger.debug("versionUri = " + versionUri);
    logger.debug("sparql query = " + query);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(webConstants.getTriplestoreBaseUri())
            .path("datasets/" + namespaceUid)
            .path(QUERY_ENDPOINT)
            .queryParam(QUERY_PARAM_QUERY, URLEncoder.encode(query, "UTF-8"))
            .build(true);

    HttpHeaders headers = new HttpHeaders();

    headers.put(X_ACCEPT_EVENT_SOURCE_VERSION, Collections.singletonList(versionUri.toString()));
    headers.put(ACCEPT_HEADER, Collections.singletonList(APPLICATION_SPARQL_RESULTS_JSON));

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  @Override
  public Map<URI, String> getStudyTitlesByUri(String namespaceUid, URI versionUri) {
    try {
      JSONArray bindings = executeQuery(namespaceUid, versionUri, STUDY_TITLES);
      return bindings
              .stream()
              .collect(Collectors.toMap(getStudyGraphUri(), getStudyTitle()));
    } catch (IOException exception) {
      throw new RuntimeException("Could not load getStudyTitles.sparql.");
    }
  }

  private Function<Object, String> getStudyTitle() {
    return binding -> JsonPath
            .read(binding, "$.title.value")
            .toString();
  }

  private Function<Object, URI> getStudyGraphUri() {
    return binding -> {
      String studyGraphUri = JsonPath
              .read(binding, "$.studyGraphUri.value")
              .toString();
      return URI.create(studyGraphUri);
    };
  }
}
