package org.drugis.addis.trialverse;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.TestUtils;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.model.TrialDataIntervention;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
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
  public void testGetOutcomes() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeResult.json");
    createMockTrialverseService(mockResult);
    List<SemanticOutcome> result = triplestoreService.getOutcomes("abc");
    SemanticOutcome result1 = new SemanticOutcome("http://trials.drugis.org/namespace/1/endpoint/test1", "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleInterventionResult.json");
    createMockTrialverseService(mockResult);

    List<SemanticIntervention> result = triplestoreService.getInterventions("abc");
    SemanticIntervention result1 = new SemanticIntervention("http://trials.drugis.org/namespace/1/drug/test1", "Azilsartan");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetDrugIds() {
    String namespaceUid = "abc";
    String studyUid = "asd";
    List<String> interventionConceptUris = new ArrayList<>();
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleDrugIdResult.json");
    createMockTrialverseService(mockResult);


    Map<Long, String> expected = new HashMap<>();
    expected.put(1L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug");
    expected.put(4L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug");

    // EXECUTOR
    Map<String, String> result = triplestoreService.getTrialverseDrugs(namespaceUid, studyUid, interventionConceptUris);

    assertEquals(expected, result);
  }

  @Test
  public void testGetOutcomeIds() {
    String namespaceUid = "abc";
    String studyUid = "asd";
    List<String> outcomeConceptUris = new ArrayList<>();
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeIdResult.json");
    createMockTrialverseService(mockResult);


    Map<Long, String> expected = new HashMap<>();
    expected.put(1L, "http://trials.drugis.org/namespace/2/endpoint/e2611534a509251f2e1c8endpoint");
    expected.put(4L, "http://trials.drugis.org/namespace/2/adverseEvent/e2611534a509251f2e1cadverseEvent");

    // EXECUTOR
    Map<String, String> result = triplestoreService.getTrialverseVariables(namespaceUid, studyUid, outcomeConceptUris);

    assertEquals(expected, result);
  }

  @Test
  public void testFindStudiesReferringToConcept() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleStudyUris.json");
    createMockTrialverseService(mockResult);

    String namespaceUid = "abc";
    List<Long> studyIds = triplestoreService.findStudiesReferringToConcept(namespaceUid, "magic");
    assertEquals(5, studyIds.size());
  }

  @Test
  public void testFindDrugConceptsFilteredByNameSpaceAndStudys() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleDrugIdResult.json");
    createMockTrialverseService(mockResult);


    String namespaceUid = "abc";
    List<String> studyIds = Arrays.asList("study1", "study2", "study3");
    List<String> interventionsURIs = Arrays.asList("http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug", "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug");
    Map<String, List<TrialDataIntervention>> result = triplestoreService.findStudyInterventions(namespaceUid, studyIds, interventionsURIs);
    assertNotNull(result);
    assertTrue(result.containsKey("study1"));
    assertTrue(result.get("study1").containsAll(Arrays.asList(new TrialDataIntervention("drugUid1", "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug", "study1"),
            new TrialDataIntervention("drugUid2", "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug", "study1"))));
  }

  @Test
  public void testGetOutComeVariableIdsByStudyForSingleOutcomeAdverseEventType() {

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeToVariableAdverseEventResult.json");
    createMockTrialverseService(mockResult);


    String namespaceUid = "abc";
    List<String> studyIds = Arrays.asList("study1", "study2", "study3");
    String outcomeConceptUri = "http://trials.drugis.org/namespace/2/adverseEvent/232145506bf409d7bb931fd35d6122c0";
    List<Pair<Long, Long>> result = triplestoreService.getOutcomeVariableUidsByStudyForSingleOutcome(namespaceUid, studyIds, outcomeConceptUri);
    assertEquals(3, result.size());
    assertTrue(result.containsAll(Arrays.asList(Pair.of("study1", 304L), Pair.of("study2", 209L), Pair.of("study3", 91L))));
  }

  @Test
  public void testGetOutComeVariableIdsByStudyForSingleOutcomeEndPointType() {

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeToVariableAdverseEventResult.json");
    createMockTrialverseService(mockResult);

    String namespaceUid = "abc";
    List<String> studyIds = Arrays.asList("study1", "study2", "study3");
    String outcomeConceptUri = "http://trials.drugis.org/namespace/2/endPoint/232145506bf409d7bb931fd35d6122c0";
    List<Pair<Long, Long>> result = triplestoreService.getOutcomeVariableUidsByStudyForSingleOutcome(namespaceUid, studyIds, outcomeConceptUri);
    assertEquals(3, result.size());
    assertTrue(result.containsAll(Arrays.asList(Pair.of("study1", 304L), Pair.of("study2", 209L), Pair.of("study3", 91L))));
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
    RestOperations restTemplate = mock(RestTemplate.class);
    when(restTemplate.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(result);
    when(restOperationsFactory.build()).thenReturn(restTemplate);

  }


}
