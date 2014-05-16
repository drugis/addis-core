package org.drugis.addis.trialverse.service;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.impl.TrialverseDataServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 13-5-14.
 */
public class TrialverseDataServiceTest {
  @Mock
  private TrialverseService trialverseService;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private TrialverseRepository trialverseRepository;

  @InjectMocks
  TrialverseDataService trialverseDataService;

  private Long namespaceId;
  private List<Study> studies;

  @Before
  public void setUp() {
    trialverseDataService = new TrialverseDataServiceImpl();
    MockitoAnnotations.initMocks(this);
    Study study1 = new Study(1L, "name", "title");
    Study study2 = new Study(2L, "name2", "title2");
    namespaceId = 1465L;
    studies = Arrays.asList(study1, study2);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(trialverseService, triplestoreService, trialverseRepository);
  }

  @Test
  public void getTrialDataByNamespaceIdAndOutcomeUri() {
    String outcomeUri = "thisisaoutcomeuri";
    List<Long> studyIds = Arrays.asList(1L, 2L, 3L);
    when(triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri)).thenReturn(studyIds);
    when(trialverseRepository.getStudiesByIds(namespaceId, studyIds)).thenReturn(studies);

    TrialData trialData = trialverseDataService.getTrialData(namespaceId, outcomeUri);

    verify(triplestoreService).findStudiesReferringToConcept(namespaceId, outcomeUri);
    verify(trialverseRepository).getStudiesByIds(namespaceId, studyIds);
    assertNotNull(trialData);
    assertNotNull(trialData.getTrialDataStudies());
    assertTrue(trialData.getTrialDataStudies().containsAll(trialData.getTrialDataStudies()));
  }

  @Test
  public void getTrialDataByWithNamespace() {
    when(trialverseRepository.queryStudies(namespaceId)).thenReturn(studies);

    TrialData trialData = trialverseDataService.getTrialData(namespaceId);

    verify(trialverseRepository).queryStudies(namespaceId);
    assertNotNull(trialData);
    assertNotNull(trialData.getTrialDataStudies());
    assertTrue(trialData.getTrialDataStudies().containsAll(trialData.getTrialDataStudies()));
  }

  @Test
  public void getTrialDataByInterventions() {
    //todo
  }

  @Test
  public void getTrialDataByOutcomeAndInterventions() {
    String outcomeUri = "outcomeUri";
    List<String> interventionUris = Arrays.asList("u1", "u2");
    List<Long> studyIds = Arrays.asList(1L, 2L, 3L);
    when(triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri)).thenReturn(studyIds);
    Map<Long, List<TrialDataIntervention>> studyInterventions = new HashMap<>();
    studyInterventions.put(1L, Arrays.asList(new TrialDataIntervention(1L, "u1", 1L), new TrialDataIntervention(1L, "u1sddgd", 1L), new TrialDataIntervention(2L, "u2", 1L)));
    List<Pair<Long, Long>> outComeVariableIdsByStudyForSingleOutcome = Arrays.asList(Pair.of(1L, 202L));
    when(triplestoreService.findStudyInterventions(namespaceId, studyIds, interventionUris)).thenReturn(studyInterventions);
    List<TrialDataArm> trialDataArms = Arrays.asList(new TrialDataArm(303L, 1L, "trailDataArm"));
    List<Measurement> measurements = Arrays.asList(new Measurement(1L, 404L, 505L, 303L, MeasurementAttribute.RATE, 123L, null));
    List<Variable> variables = ListUtils.EMPTY_LIST;
    List<Long> variableIds = Arrays.asList(202L);
    when(trialverseRepository.getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()))).thenReturn(studies.subList(0, 1));
    when(triplestoreService.getOutcomeVariableIdsByStudyForSingleOutcome(namespaceId, studyIds, outcomeUri)).thenReturn(outComeVariableIdsByStudyForSingleOutcome);
    when(trialverseRepository.getArmsForStudies(namespaceId, studyIds, variables)).thenReturn(trialDataArms);
    when(trialverseRepository.getStudyMeasurementsForOutcomes(new ArrayList<>(studyInterventions.keySet()), variableIds, Arrays.asList(303L))).thenReturn(measurements);
    when(trialverseRepository.getVariablesByOutcomeIds(variableIds)).thenReturn(variables);

    TrialData trialData = trialverseDataService.getTrialData(namespaceId, outcomeUri, interventionUris);

    verify(triplestoreService).findStudiesReferringToConcept(namespaceId, outcomeUri);
    verify(triplestoreService).findStudyInterventions(namespaceId, studyIds, interventionUris);
    verify(trialverseRepository).getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()));
    verify(triplestoreService).getOutcomeVariableIdsByStudyForSingleOutcome(namespaceId, studyIds, outcomeUri);
    verify(trialverseRepository).getArmsForStudies(namespaceId, studyIds, variables);
    Map<Long, String> tempMap = new HashMap<>();
    tempMap.put(303L, "anyString");
    verify(trialverseRepository).getStudyMeasurementsForOutcomes(studyIds, variableIds, tempMap.keySet());
    verify(trialverseRepository).getVariablesByOutcomeIds(variableIds);

    assertNotNull(trialData);
    assertNotNull(trialData.getTrialDataStudies());
    assertTrue(trialData.getTrialDataStudies().containsAll(trialData.getTrialDataStudies()));
  }
}
