package org.drugis.addis.trialverse;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.TestUtils;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 2/28/14.
 */

public class TriplestoreServiceTest {

  @Mock
  RestOperationsFactory restOperationsFactory;

  @InjectMocks
  TriplestoreService triplestoreService;

  @Before
  public void setUp() {
    triplestoreService = new TriplestoreServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testQueryNamespaces() throws ParseException {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleQueryNamespacesResult.json");
    ResponseEntity<String> resultEntity = new ResponseEntity<String>(mockResult, HttpStatus.OK);
    RestOperations restTemplate = mock(RestTemplate.class);

    UriComponents uriComponents = UriComponentsBuilder
            .fromHttpUrl(TriplestoreService.TRIPLESTORE_BASE_URI)
            .path("datasets/")
            .build();

    when(restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, TriplestoreServiceImpl.acceptJsonRequest, String.class)).thenReturn(resultEntity);
    String datasetUuid = "d1";
    String query = TriplestoreServiceImpl.NAMESPACE;
    UriComponents uriComponents2 = UriComponentsBuilder.fromHttpUrl(TriplestoreService.TRIPLESTORE_BASE_URI)
            .path("datasets/" + datasetUuid)
            .path(TriplestoreServiceImpl.QUERY_ENDPOINT)
            .queryParam(TriplestoreServiceImpl.QUERY_PARAM_QUERY, query)
            .build();
    String mockResult2 = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleGetNamespaceResult.json");
    MultiValueMap<String, String> responceHeaders = new HttpHeaders();
    responceHeaders.add(TriplestoreServiceImpl.X_EVENT_SOURCE_VERSION, "version");
    ResponseEntity<String> resultEntity2 = new ResponseEntity<String>(mockResult2, responceHeaders, HttpStatus.OK);
    when(restTemplate.exchange(uriComponents2.toUri(), HttpMethod.GET, TriplestoreServiceImpl.acceptSpaqlResultsRequest, String.class)).thenReturn(resultEntity2);
    when(restOperationsFactory.build()).thenReturn(restTemplate);

    Collection<Namespace> namespaces = triplestoreService.queryNameSpaces();

    assertEquals(1, namespaces.size());
  }

  @Test
  public void testGetOutcomes() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeResult.json");
    createMockTrialverseService(mockResult);
    List<SemanticOutcome> result = triplestoreService.getOutcomes("abc", "version");
    SemanticOutcome result1 = new SemanticOutcome("fdszgs-adsfd-1", "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleInterventionResult.json");
    createMockTrialverseService(mockResult);

    List<SemanticIntervention> result = triplestoreService.getInterventions("abc", "version");
    SemanticIntervention intervention = result.get(0);
    SemanticIntervention expectedSemanticIntervention = new SemanticIntervention("fdhdfgh-saddsgfsdf-123-a", "Azilsartan");
    assertEquals(expectedSemanticIntervention, intervention);
  }


  @Test
  public void testQueryStudydetails() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleQueryStudyDetailsResult.json");
    createMockTrialverseService(mockResult);

    String namespaceUid = "namespaceUid";
    List<StudyWithDetails> studyWithDetailsList = triplestoreService.queryStudydetailsHead(namespaceUid);

    assertNotNull(studyWithDetailsList);
    assertEquals(2, studyWithDetailsList.size());
    StudyWithDetails studyWithDetailsNoStartOrEndDate = studyWithDetailsList.get(0);

    assertNotNull(studyWithDetailsNoStartOrEndDate.getStudyUid());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getName());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getTitle());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getAllocation());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getBlinding());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getInclusionCriteria());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getNumberOfStudyCenters());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getPubmedUrls());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getStatus());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getIndication());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getObjectives());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getInvestigationalDrugNames());
    assertNotNull(studyWithDetailsNoStartOrEndDate.getNumberOfArms());

    assertNull(studyWithDetailsNoStartOrEndDate.getStartDate());
    assertNull(studyWithDetailsNoStartOrEndDate.getEndDate());

    StudyWithDetails studyWithDetailsWithStartAndEndDate = studyWithDetailsList.get(1);
    assertNotNull(studyWithDetailsWithStartAndEndDate.getStartDate());
    assertNotNull(studyWithDetailsWithStartAndEndDate.getEndDate());
  }

  @Test
  public void testGetStudyDetails() throws ResourceDoesNotExistException {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleGetStudyDetailsResult.json");
    createMockTrialverseService(mockResult);

    StudyWithDetails studyWithDetails = triplestoreService.getStudydetails(namespaceUid, "version");

    assertNotNull(studyWithDetails.getStudyUid());
    assertNotNull(studyWithDetails.getName());
    assertNotNull(studyWithDetails.getTitle());
  }

  @Test
  public void testGetStudyArms() throws ResourceDoesNotExistException {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleStudyArmsResult.json");
    createMockTrialverseService(mockResult);

    JSONArray arms = triplestoreService.getStudyArms(namespaceUid, "version");

    assertEquals(3, arms.size());
    JSONObject jsonObject = (JSONObject) arms.get(0);
    assertTrue(jsonObject.containsKey("arm"));
    assertTrue(jsonObject.containsKey("armLabel"));
    assertTrue(jsonObject.containsKey("numberOfParticipantsStarting"));

    assertTrue(jsonObject.containsValue("http://trials.drugis.org/instances/5959fd08-9c5b-4016-8118-d195cdb80c70"));
    assertTrue(jsonObject.containsValue("Olmesartan medoxomil 20-40mg/hydrochlorothiazide 12.5-25mg QD"));
    assertTrue(jsonObject.containsValue("356"));
  }

  @Test
  public void testGetTreatmentActivities() {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleStudyTreatmentActivitiesResult.json");
    createMockTrialverseService(mockResult);

    List<TreatmentActivity> treatmentActivities = triplestoreService.getStudyTreatmentActivities(namespaceUid, "version");
    assertEquals(4, treatmentActivities.size());
  }

  @Test
  public void testGetStudyDataForBaseLineCharacteristics() {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    StudyDataSection studyDataSection = StudyDataSection.BASE_LINE_CHARACTERISTICS;

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleBaseLineCharacteristicsResult.json");
    createMockTrialverseService(mockResult);

    List<StudyData> result = triplestoreService.getStudyData(namespaceUid, studyUid, studyDataSection);
    assertEquals(2, result.size());
  }

  @Test
  public void testGetStudyDataForEndPoints() {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    StudyDataSection studyDataSection = StudyDataSection.ENDPOINTS;

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleEndPointResult.json");
    createMockTrialverseService(mockResult);

    List<StudyData> result = triplestoreService.getStudyData(namespaceUid, studyUid, studyDataSection);
    assertEquals(15, result.size());
  }

  @Test
  public void testQueryStudies() {
    String namespaceUid = "namespaceUid";
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleQueryStudiesResult.json");
    createMockTrialverseService(mockResult);

    List<Study> result = triplestoreService.queryStudies(namespaceUid, "version");
    assertEquals(5, result.size()); // epar example
    assertEquals(3, result.get(4).getTreatmentArms().size());
    // this one should have a multi drug arm
    assertEquals(2, ((StudyTreatmentArm) result.get(4).getTreatmentArms().toArray()[2]).getInterventionUids().size());
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
    ResponseEntity<String> resultEntity = new ResponseEntity<String>(result, HttpStatus.OK);
    RestOperations restTemplate = mock(RestTemplate.class);
//    when(restTemplate.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(result);
    when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resultEntity);
    when(restOperationsFactory.build()).thenReturn(restTemplate);
  }

}
