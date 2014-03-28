package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.AlternativeService;
import org.drugis.addis.problems.service.CriteriaService;
import org.drugis.addis.problems.service.PerformanceTableService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.problems.service.model.ContinuousMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TrialverseServiceImpl;
import org.drugis.addis.util.JSONUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  AlternativeService alternativeService;

  @Mock
  CriteriaService criteriaService;

  @Mock
  PerformanceTableService performanceTableService;

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
    verifyNoMoreInteractions(projectRepository, analysisRepository, alternativeService, criteriaService);
  }

  @Test
  public void testGetProblem() throws ResourceDoesNotExistException {

    int projectId = 1;
    Project project = mock(Project.class);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);

    int analysisId = 2;
    Analysis analysis = mock(Analysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);

    Map<Long, AlternativeEntry> alternativesCache = new HashMap<>();
    long alternativeEntryKey = 3L;
    AlternativeEntry alternativeEntry = mock(AlternativeEntry.class);
    alternativesCache.put(alternativeEntryKey, alternativeEntry);
    when(alternativeService.createAlternatives(project, analysis)).thenReturn(alternativesCache);

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = new ArrayList<>();
    Variable variable = mock(Variable.class);
    CriterionEntry criterionEntry = mock(CriterionEntry.class);
    Pair<Variable, CriterionEntry> variableCriterionPair = new ImmutablePair<>(variable, criterionEntry);
    variableCriteriaPairs.add(variableCriterionPair);
    when(criteriaService.createVariableCriteriaPairs(project, analysis)).thenReturn(variableCriteriaPairs);

    long variableId = 4L;
    Map<Long, CriterionEntry> criteraCache = new HashMap<>();
    criteraCache.put(variableId, criterionEntry);
    List<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    AbstractMeasurementEntry measurementEntry = mock(ContinuousMeasurementEntry.class);
    performanceTable.add(measurementEntry);
    when(performanceTableService.createPerformaceTable(project, analysis, alternativesCache, criteraCache)).thenReturn(performanceTable);

    when(variable.getId()).thenReturn(variableId);

    // execute
    Problem actualProblem = problemService.getProblem(projectId, analysisId);

    verify(projectRepository).getProjectById(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(alternativeService).createAlternatives(project, analysis);
    verify(criteriaService).createVariableCriteriaPairs(project, analysis);
    verify(performanceTableService).createPerformaceTable(project, analysis, alternativesCache, criteraCache);

    assertNotNull(actualProblem);
  }

}
