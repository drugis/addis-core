package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
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
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TrialverseService;
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
  AnalysisRepository analysisRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  AlternativeService alternativeService;

  @Mock
  CriteriaService criteriaService;

  @Mock
  MeasurementsService measurementsService;

  @Mock
  PerformanceTableBuilder performanceTablebuilder;

  @Mock
  InterventionRepository interventionRepository;

  @Mock
  TrialverseService trialverseService;

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
    verifyNoMoreInteractions(analysisRepository, projectRepository, singleStudyBenefitRiskAnalysisRepository,
      alternativeService, criteriaService, interventionRepository, trialverseService);
  }

  @Test
  public void testGetSingleStudyBenefitRiskProblem() throws ResourceDoesNotExistException {

    int projectId = 1;
    Project project = mock(Project.class);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);

    int analysisId = 2;
    SingleStudyBenefitRiskAnalysis analysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
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
    SingleStudyBenefitRiskProblem actualProblem = (SingleStudyBenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).getProjectById(projectId);
    verify(analysisRepository).get(projectId, analysisId);
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

  @Test
  public void testGetNetworkMetaAnalysisProblem() throws ResourceDoesNotExistException {
    Long namespaceId = 1L;
    Integer projectId = 2;
    Integer analysisId = 3;
    Long studyId = 101L;
    Long drugId1 = 420L;
    Long drugId2 = 430L;
    Long armId1 = 555L;
    Long armId2 = 666L;
    Long armId3 = 777L;
    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticOutcome(outcomeUri, "label3"));
    AbstractAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", outcome);
    Project project = mock(Project.class);
    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    Intervention intervention1 = new Intervention(1, projectId, "int1", "moti", semanticIntervention1);
    Intervention intervention2 = new Intervention(2, projectId, "int2", "moti", semanticIntervention2);
    Collection<Intervention> interventions = Arrays.asList(intervention1, intervention2);
    ObjectMapper mapper = new ObjectMapper();

    TrialDataIntervention trialDataIntervention1 = new TrialDataIntervention(drugId1, "uri1", studyId);
    TrialDataIntervention trialDataIntervention2 = new TrialDataIntervention(drugId2, "uri2", studyId);
    List<TrialDataIntervention> trialdataInterventions = Arrays.asList(trialDataIntervention1, trialDataIntervention2);

    Measurement measurement1 = new Measurement(studyId, 333L, 444L, armId1,MeasurementAttribute.SAMPLE_SIZE, 768784L, null);
    Measurement measurement2 = new Measurement(studyId, 333L, 444L, armId2,MeasurementAttribute.STANDARD_DEVIATION, null, Math.E);
    Measurement measurement3 = new Measurement(studyId, 333L, 444L, armId2,MeasurementAttribute.MEAN, null, Math.PI);

    Measurement measurement4 = new Measurement(studyId, 333L, 444L, armId3, MeasurementAttribute.SAMPLE_SIZE, -1L, null);
    Measurement measurement5 = new Measurement(studyId, 333L, 444L, armId3, MeasurementAttribute.RATE, -1L, null);

    List<Measurement> measurements1 = Arrays.asList(measurement1, measurement2, measurement3);
    List<Measurement> measurements2 = Arrays.asList(measurement1, measurement2, measurement3);
    List<Measurement> measurements3 = Arrays.asList(measurement4, measurement5);

    TrialDataArm trialDataArm1 = new TrialDataArm(armId1, studyId, "arm bb", drugId1, measurements1);
    TrialDataArm trialDataArm2 = new TrialDataArm(armId2, studyId, "arm aa", drugId2, measurements2);
    TrialDataArm trialDataArm3 = new TrialDataArm(armId3, studyId, "aaa", drugId2, measurements3);
    List<TrialDataArm> trialDataArms = Arrays.asList(trialDataArm1, trialDataArm2, trialDataArm3);
    TrialDataStudy trialDataStudy1 = new TrialDataStudy(1L, "study1", trialdataInterventions, trialDataArms);
    List<TrialDataStudy> trialDataStudies = Arrays.asList(trialDataStudy1);
    TrialData trialData = new TrialData(trialDataStudies);
    ObjectNode trialdataNode = mapper.convertValue(trialData, ObjectNode.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getTrialverseId()).thenReturn(namespaceId.intValue());
    when(projectRepository.getProjectById(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(trialverseService.getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri2"))).thenReturn(trialdataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).getProjectById(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri2"));

    assertNotNull(problem);
    assertEquals(2, problem.getEntries().size());
    ContinuousNetworkMetaAnalysisProblemEntry entry = new ContinuousNetworkMetaAnalysisProblemEntry("study1", "int1", 768784L, Math.PI, Math.E);
    assertTrue(problem.getEntries().contains(entry));

    // expect the measurements from arm 3 to be uses as arms using the sames drug ara sorted by alphabet and the first one is used
    RateNetworkMetaAnalysisProblemEntry rateNetworkMetaAnalysisProblemEntry = (RateNetworkMetaAnalysisProblemEntry) problem.getEntries().get(0);
    assertEquals(-1L, rateNetworkMetaAnalysisProblemEntry.getResponders().longValue());
  }

}
