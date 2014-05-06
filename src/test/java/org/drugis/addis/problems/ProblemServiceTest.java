package org.drugis.addis.problems;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.AlternativeService;
import org.drugis.addis.problems.service.CriteriaService;
import org.drugis.addis.problems.service.MeasurementsService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.problems.service.model.ContinuousMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.util.JSONUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  AlternativeService alternativeService;

  @Mock
  CriteriaService criteriaService;

  @Mock
  MeasurementsService measurementsService;

  @Mock
  private PerformanceTableBuilder performanceTablebuilder;

  @Mock
  JSONUtils jsonUtils;

  @InjectMocks
  ProblemService problemService;

  @Before
  public void setUp() {
    problemService = new ProblemServiceImpl();
    alternativeService = new AlternativeService();
    criteriaService = new CriteriaService();
    jsonUtils = new JSONUtils();
    MockitoAnnotations.initMocks(this);

    when(jsonUtils.createKey(anyString())).thenReturn("key1", "key2", "key3");
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(projectRepository, singleStudyBenefitRiskAnalysisRepository, alternativeService, criteriaService);
  }

  @Test
  public void testGetProblem() throws ResourceDoesNotExistException {

    int projectId = 1;
    Project project = mock(Project.class);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);

    int analysisId = 2;
    SingleStudyBenefitRiskAnalysis analysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(singleStudyBenefitRiskAnalysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(analysis.getName()).thenReturn("analysisName");

    Map<Long, AlternativeEntry> alternativesCache = new HashMap<>();
    long alternativeEntryKey = 3L;
    AlternativeEntry alternativeEntry = mock(AlternativeEntry.class);
    alternativesCache.put(alternativeEntryKey, alternativeEntry);
    when(alternativeService.createAlternatives(project, analysis)).thenReturn(alternativesCache);

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = new ArrayList<>();
    Variable variable = mock(Variable.class);
    CriterionEntry criterionEntry = mock(CriterionEntry.class);
    String criterionEntryTitle = "Criterion entry";
    when(criterionEntry.getTitle()).thenReturn(criterionEntryTitle);
    String mockKey = "mockKey";
    when(jsonUtils.createKey(criterionEntryTitle)).thenReturn(mockKey);
    Pair<Variable, CriterionEntry> variableCriterionPair = new ImmutablePair<>(variable, criterionEntry);
    variableCriteriaPairs.add(variableCriterionPair);
    when(criteriaService.createVariableCriteriaPairs(project, analysis)).thenReturn(variableCriteriaPairs);

    long variableId = 4L;
    Map<Long, CriterionEntry> criteriaCache = new HashMap<>();
    criteriaCache.put(variableId, criterionEntry);
    List<Measurement> measurements = new ArrayList<>();
    Measurement measurement = mock(Measurement.class);
    measurements.add(measurement);
    when(measurementsService.createMeasurements(project, analysis, alternativesCache)).thenReturn(measurements);

    when(variable.getId()).thenReturn(variableId);

    AbstractMeasurementEntry measurementEntry = mock(ContinuousMeasurementEntry.class);
    List<AbstractMeasurementEntry> performanceTable = Arrays.asList(measurementEntry);
    when(performanceTablebuilder.build(criteriaCache, alternativesCache, measurements)).thenReturn(performanceTable);

    // execute
    Problem actualProblem = problemService.getProblem(projectId, analysisId);

    verify(projectRepository).getProjectById(projectId);
    verify(singleStudyBenefitRiskAnalysisRepository).get(projectId, analysisId);
    verify(alternativeService).createAlternatives(project, analysis);
    verify(criteriaService).createVariableCriteriaPairs(project, analysis);
    verify(measurementsService).createMeasurements(project, analysis, alternativesCache);
    verify(performanceTablebuilder).build(criteriaCache, alternativesCache, measurements);
    verify(jsonUtils, times(2)).createKey(anyString());

    assertNotNull(actualProblem);
    assertNotNull(actualProblem.getTitle());
    assertEquals(analysis.getName(), actualProblem.getTitle());
    assertNotNull(actualProblem.getAlternatives());
    assertNotNull(actualProblem.getCriteria());

    Map<String, CriterionEntry> actualCriteria = actualProblem.getCriteria();
    assertTrue(actualCriteria.keySet().contains(mockKey));
    verify(jsonUtils).createKey(criterionEntryTitle);
  }

}
