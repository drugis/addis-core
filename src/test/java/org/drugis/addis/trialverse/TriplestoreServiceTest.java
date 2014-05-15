package org.drugis.addis.trialverse;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.TestUtils;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 2/28/14.
 */

public class TriplestoreServiceTest {

  @Mock
  RestTemplate triplestoreMock;

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
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticOutcome> result = triplestoreService.getOutcomes(1L);
    SemanticOutcome result1 = new SemanticOutcome("http://trials.drugis.org/namespace/1/endpoint/test1", "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleInterventionResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticIntervention> result = triplestoreService.getInterventions(1L);
    SemanticIntervention result1 = new SemanticIntervention("http://trials.drugis.org/namespace/1/drug/test1", "Azilsartan");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetDrugIds() {
    Long namespaceId = 1L;
    Long studyId = 1L;
    List<String> interventionConceptUris = new ArrayList<>();
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleDrugIdResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);

    Map<Long, String> expected = new HashMap<>();
    expected.put(1L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug");
    expected.put(4L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug");

    // EXECUTOR
    Map<Long, String> result = triplestoreService.getTrialverseDrugs(namespaceId, studyId, interventionConceptUris);

    assertEquals(expected, result);
  }

  @Test
  public void testGetOutcomeIds() {
    Long namespaceId = 1L;
    Long studyId = 1L;
    List<String> outcomeConceptUris = new ArrayList<>();
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeIdResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);

    Map<Long, String> expected = new HashMap<>();
    expected.put(1L, "http://trials.drugis.org/namespace/2/endpoint/e2611534a509251f2e1c8endpoint");
    expected.put(4L, "http://trials.drugis.org/namespace/2/adverseEvent/e2611534a509251f2e1cadverseEvent");

    // EXECUTOR
    Map<Long, String> result = triplestoreService.getTrialverseVariables(namespaceId, studyId, outcomeConceptUris);

    assertEquals(expected, result);
  }

  @Test
  public void testFindStudiesReferringToConcept() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleStudyUris.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    Long namespaceId = 1L;
    List<Long> studyIds = triplestoreService.findStudiesReferringToConcept(namespaceId, "magic");
    assertEquals(5, studyIds.size());
  }

  @Test
  public void testFindDrugConceptsFilteredByNameSpaceAndStudys() {
    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleDrugIdResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);

    Long namespaceId = 1L;
    List<Long> studyIds = Arrays.asList(1L, 2L, 3L);
    List<String> interventionsURIs = Arrays.asList("http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug", "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug");
    Map<Long, List<Pair<Long, String>>> result = triplestoreService.findStudyInterventions(namespaceId, studyIds, interventionsURIs);
    assertNotNull(result);
    assertTrue(result.containsKey(1L));
    assertTrue(result.get(1L).containsAll(Arrays.asList(Pair.of(1L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cdrug"), Pair.of(4L, "http://trials.drugis.org/namespace/2/drug/e2611534a509251f2e1cnogeendrug"))));
  }

  @Test
  public void testGetOutComeVariableIdsByStudyForSingleOutcomeAdverseEventType() {

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeToVariableAdverseEventResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);

    Long namespaceId = 1L;
    List<Long> studyIds = Arrays.asList(20L, 13L, 5L);
    String outcomeConceptUri = "http://trials.drugis.org/namespace/2/adverseEvent/232145506bf409d7bb931fd35d6122c0";
    List<Pair<Long, Long>> result = triplestoreService.getOutComeVariableIdsByStudyForSingleOutcome(namespaceId, studyIds, outcomeConceptUri);
    assertEquals(3, result.size());
    assertTrue(result.containsAll(Arrays.asList(Pair.of(20L, 304L), Pair.of(13L, 209L), Pair.of(5L, 91L))));
  }

  @Test
  public void testGetOutComeVariableIdsByStudyForSingleOutcomeEndPointType() {

    String mockResult = TestUtils.loadResource(this.getClass(), "/triplestoreService/exampleOutcomeToVariableAdverseEventResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);

    Long namespaceId = 1L;
    List<Long> studyIds = Arrays.asList(20L, 13L, 5L);
    String outcomeConceptUri = "http://trials.drugis.org/namespace/2/endPoint/232145506bf409d7bb931fd35d6122c0";
    List<Pair<Long, Long>> result = triplestoreService.getOutComeVariableIdsByStudyForSingleOutcome(namespaceId, studyIds, outcomeConceptUri);
    assertEquals(3, result.size());
    assertTrue(result.containsAll(Arrays.asList(Pair.of(20L, 304L), Pair.of(13L, 209L), Pair.of(5L, 91L))));
  }

}
