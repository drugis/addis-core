package org.drugis.addis.trialverse;

import net.minidev.json.parser.ParseException;
import org.drugis.addis.TestUtils;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.drugis.addis.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.drugis.addis.util.WebConstants.ACCEPT_HEADER;
import static org.drugis.addis.util.WebConstants.APPLICATION_SPARQL_RESULTS_JSON;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TriplestoreServiceTest {

  private final URI version = URI.create("http://versions.com/version");

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private CovariateRepository covariateRepository;

  @Mock
  private InterventionRepository interventionRepository;

  @Mock
  private QueryResultMappingService queryResultMappingService;

  @Mock
  private  InterventionService interventionService;

  @Mock
  private  WebConstants webConstants;

  @InjectMocks
  private TriplestoreService triplestoreService;
  private final String TRIPLESTORE_BASE_URI = "http://jena-es:8080";

  @Before
  public void setUp() {
    triplestoreService = new TriplestoreServiceImpl();
    MockitoAnnotations.initMocks(this);
    when(webConstants.getTriplestoreBaseUri()).thenReturn(TRIPLESTORE_BASE_URI);
  }

  @Test
  public void testQueryNamespaces() throws ParseException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleQueryNamespacesResult.json");
    ResponseEntity<String> resultEntity = new ResponseEntity<>(mockResult, HttpStatus.OK);

    UriComponents uriComponents = UriComponentsBuilder
            .fromHttpUrl(webConstants.getTriplestoreBaseUri())
            .path("datasets/")
            .build();

    String datasetUuid = "d1";
    HttpHeaders acceptSparqlHeaders = new HttpHeaders();
    acceptSparqlHeaders.put(ACCEPT_HEADER, singletonList(APPLICATION_SPARQL_RESULTS_JSON));
    final HttpEntity<String> namespaceRequest = new HttpEntity<>(TriplestoreServiceImpl.NAMESPACE, acceptSparqlHeaders);

    UriComponents uriComponents2 = UriComponentsBuilder.fromHttpUrl(webConstants.getTriplestoreBaseUri())
            .path("datasets/" + datasetUuid)
            .path(TriplestoreServiceImpl.QUERY_ENDPOINT)
            .build();
    String mockResult2 = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleGetNamespaceResult.json");
    MultiValueMap<String, String> responseHeaders = new HttpHeaders();
    responseHeaders.add(TriplestoreServiceImpl.X_EVENT_SOURCE_VERSION, "version");
    ResponseEntity<String> resultEntity2 = new ResponseEntity<>(mockResult2, responseHeaders, HttpStatus.OK);
    String graphBody = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleNamespaceResult.ttl");
    ResponseEntity<String> resultEntity3 = new ResponseEntity<>(graphBody, responseHeaders, HttpStatus.OK);

    when(restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, TriplestoreServiceImpl.acceptJsonRequest, String.class)).thenReturn(resultEntity);
    when(restTemplate.exchange(uriComponents2.toUri(), HttpMethod.POST, namespaceRequest, String.class)).thenReturn(resultEntity2);
    HttpHeaders headers = new HttpHeaders();
    headers.set(ACCEPT_HEADER, "text/turtle,text/html");
    URI datasetUrl = URI.create(TRIPLESTORE_BASE_URI + "/datasets/ds1");
    when(webConstants.buildDatasetUri(datasetUuid)).thenReturn(datasetUrl);
    when(restTemplate.exchange(datasetUrl, HttpMethod.GET, new HttpEntity<String>(headers), String.class)).thenReturn(resultEntity3);

    Collection<Namespace> namespaces = triplestoreService.queryNameSpaces();
    assertEquals(1, namespaces.size());
  }

  @Test
  public void testGetOutcomes() throws ReadValueException, IOException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeResult.json");
    createMockTrialverseService(mockResult);
    List<SemanticVariable> result = triplestoreService.getOutcomes("abc", version);
    SemanticVariable result1 = new SemanticVariable(URI.create("http://trials.drugis.org/namespace/1/endpoint/fdszgs-adsfd-1"), "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetPopulationCharacteristics() throws ReadValueException, IOException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/examplePopulationCharacteristicResult.json");
    createMockTrialverseService(mockResult);
    List<SemanticVariable> result = triplestoreService.getPopulationCharacteristics("abc", version);
    SemanticVariable result1 = new SemanticVariable(URI.create("http://trials.drugis.org/namespace/1/populationCharacteristic/fdszgs-adsfd-1"), "Age");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() throws IOException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleInterventionResult.json");
    createMockTrialverseService(mockResult);

    List<SemanticInterventionUriAndName> result = triplestoreService.getInterventions("abc", version);
    SemanticInterventionUriAndName intervention = result.get(0);
    SemanticInterventionUriAndName expectedSemanticInterventionUuidAndName = new SemanticInterventionUriAndName(URI.create("http://trials.drugis.org/namespace/1/drug/fdhdfgh-saddsgfsdf-123-a"), "Azilsartan");
    assertEquals(expectedSemanticInterventionUuidAndName, intervention);
  }

  @Test
  public void testGetUnits() throws IOException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleUnitsResult.json");
    createMockTrialverseService(mockResult);
    List<URI> unitUris = triplestoreService.getUnitUris("abc", version);
    assertEquals(2, unitUris.size());
    assertEquals(URI.create("http://trials.drugis.org/concepts/1"), unitUris.get(0));
  }

  @Test
  public void testQueryStudies() throws IOException {
    String namespaceUid = "namespaceUid";
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleQueryStudiesResult.json");
    createMockTrialverseService(mockResult);

    List<Study> result = triplestoreService.queryStudies(namespaceUid, version);
    assertEquals(5, result.size()); // epar example
    assertEquals(3, result.get(0).getTreatmentArms().size());

    for (Study study : result) {
      if (study.getTitle().equals("TAK491-301 / NCT00846365")) {
        for (StudyTreatmentArm arm : study.getTreatmentArms()) {
          if (arm.getArmUid().equals("http://trials.drugis.org/instances/5b9bb252-59d3-4936-a005-7ee443878c43")) {
            // this one should have a multi drug arm
            arm.getInterventionUids().size();
          }
        }
      }
    }
  }

  @Test
  public void getCovariateTestData() throws ReadValueException, IOException {
    String namespaceUid = "namespaceUid";
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/covariateDataExample.json");
    createMockTrialverseService(mockResult);

    List<CovariateStudyValue> result = triplestoreService.getStudyLevelCovariateValues(namespaceUid, version, Collections.singletonList(CovariateOption.ALLOCATION_RANDOMIZED));
    assertEquals(4, result.size());

  }

  @Test
  public void testRegEx() {
    String studyOptionsString = "1|2";
    String uri1 = "foo/study/1/whatevr";
    String uri10 = "foo/study/10/whatevr";
    String uri12 = "foo/study/12/whatevr";
    String reg = "/study/(" + studyOptionsString + ")/";
    Pattern pattern = Pattern.compile(reg);
    Matcher matcher1 = pattern.matcher(uri1);
    Matcher matcher10 = pattern.matcher(uri10);
    Matcher matcher12 = pattern.matcher(uri12);
    assertTrue(matcher1.find());
    assertFalse(matcher10.find());
    assertFalse(matcher12.find());
  }

  private void createMockTrialverseService(String result) {
    ResponseEntity resultEntity = new ResponseEntity<>(result, HttpStatus.OK);
    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resultEntity);
  }


  @Test
  public void testAddMatchingInformation() throws ResourceDoesNotExistException, InvalidTypeForDoseCheckException {
    URI armUri = URI.create("armUri");
    TrialDataArm arm = new TrialDataArm(armUri, "arm 1");
    URI drugConcept = URI.create("conceptInterventionUri");
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(armUri, drugConcept);
    arm.addSemanticIntervention(semanticIntervention);
    List<TrialDataArm> arms = Collections.singletonList(arm);
    URI studyUri = URI.create("studyUri");
    TrialDataStudy study = new TrialDataStudy(studyUri, "study 1", arms);
    List<TrialDataStudy> studies = Collections.singletonList(study);
    AbstractIntervention intervention = mock(SimpleIntervention.class);
    when(intervention.getId()).thenReturn(1337);
    Set<AbstractIntervention> includedInterventions = new HashSet<>();
    includedInterventions.add(intervention);
    when(interventionService.isMatched(intervention, arm.getSemanticInterventions())).thenReturn(true);
    triplestoreService.addMatchingInformation(includedInterventions, studies);
  }


}
