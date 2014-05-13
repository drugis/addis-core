package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.Study;
import org.drugis.addis.trialverse.model.TrialData;
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
    assertNotNull(trialData.getStudies());
    assertTrue(trialData.getStudies().containsAll(studies));
  }

  @Test
  public void getTrialDataByWithNamespace() {
    when(trialverseRepository.queryStudies(namespaceId)).thenReturn(studies);

    TrialData trialData = trialverseDataService.getTrialData(namespaceId);

    verify(trialverseRepository).queryStudies(namespaceId);
    assertNotNull(trialData);
    assertNotNull(trialData.getStudies());
    assertTrue(trialData.getStudies().containsAll(studies));
  }

  @Test
  public void getTrialDataByInterventions() {
    List<String> interventionUris = Arrays.asList("u1", "u2");
    //todo
    //  when(trialverseRepository.queryStudies())
    TrialData trialData = trialverseDataService.getTrialData(namespaceId, interventionUris);
  }

  @Test
  public void getTrialDataByOutcomeAndInterventions() {
    String outcomeUri = "outcomeUri";
    List<String> interventionUris = Arrays.asList("u1", "u2");
    List<Long> studyIds = Arrays.asList(1L, 2L, 3L);
    when(triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri)).thenReturn(studyIds);
    Map<Long, List<Long>> studyInterventions = new HashMap<>();
    studyInterventions.put(1L, Arrays.asList(1L, 2L, 3L));
    when(triplestoreService.findStudyInterventions(namespaceId, studyIds, interventionUris)).thenReturn(studyInterventions);
    when(trialverseRepository.getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()))).thenReturn(studies);

    TrialData trialData = trialverseDataService.getTrialData(namespaceId, outcomeUri, interventionUris);
    verify(triplestoreService).findStudiesReferringToConcept(namespaceId, outcomeUri);
    verify(triplestoreService).findStudyInterventions(namespaceId, studyIds, interventionUris);
    verify(trialverseRepository).getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()));
    assertNotNull(trialData);
    assertNotNull(trialData.getStudies());
    assertTrue(trialData.getStudies().containsAll(studies));
  }
}
