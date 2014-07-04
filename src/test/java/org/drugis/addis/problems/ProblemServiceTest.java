package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.*;
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

import static org.junit.Assert.*;
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
    when(projectRepository.get(projectId)).thenReturn(project);

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

    verify(projectRepository).get(projectId);
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

    TrialData trialData = createMockTrialData();

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticOutcome(outcomeUri, "label3"));
    ArmExclusion armExclusion1 = new ArmExclusion(analysisId, 888L); // trialDataArm with armId4
    List<ArmExclusion> armExclusions = Arrays.asList(armExclusion1);

    Project project = mock(Project.class);
    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    Intervention intervention1 = new Intervention(1, projectId, "int1", "moti", semanticIntervention1);
    Intervention intervention2 = new Intervention(2, projectId, "int2", "moti", semanticIntervention2);
    Intervention intervention3 = new Intervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysisId, intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysisId, intervention2.getId());
    InterventionInclusion interventionInclusion3 = new InterventionInclusion(analysisId, intervention3.getId());
    List<InterventionInclusion> interventionInclusions = Arrays.asList(interventionInclusion1, interventionInclusion2, interventionInclusion3);

    AbstractAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", armExclusions, interventionInclusions, outcome);

    ObjectMapper mapper = new ObjectMapper();

    ObjectNode trialDataNode = mapper.convertValue(trialData, ObjectNode.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getTrialverseId()).thenReturn(namespaceId.intValue());
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(trialverseService.getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"))).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"));

    assertNotNull(problem);
    assertEquals(3, problem.getEntries().size());
    ContinuousNetworkMetaAnalysisProblemEntry entry = new ContinuousNetworkMetaAnalysisProblemEntry("study1", "int1", 768784L, Math.PI, Math.E);
    assertTrue(problem.getEntries().contains(entry));
  }

  @Test
  public void testGetNetworkAnalysisProblemWithInterventionInclusions() throws ResourceDoesNotExistException {
    Long namespaceId = 1L;
    Integer projectId = 2;
    Integer analysisId = 3;

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticOutcome(outcomeUri, "label3"));

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    Intervention intervention1 = new Intervention(1, projectId, "int1", "moti", semanticIntervention1);
    Intervention intervention2 = new Intervention(2, projectId, "int2", "moti", semanticIntervention2);
    Intervention intervention3 = new Intervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getTrialverseId()).thenReturn(namespaceId.intValue());

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysisId, intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysisId, intervention3.getId());
    List<InterventionInclusion> interventionInclusions = Arrays.asList(interventionInclusion1, interventionInclusion2);
    AbstractAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", Collections.EMPTY_LIST, interventionInclusions, outcome);


    TrialData trialData = createMockTrialData();
    TrialDataStudy trialDataStudy = trialData.getTrialDataStudies().get(0);
    List<TrialDataIntervention> trialDataInterventions = trialDataStudy.getTrialDataInterventions();
    // remove excluded intervention from trialdata as well (HACKY)
    trialDataInterventions.set(1, new TrialDataIntervention(-666L, "iamnothere", -666L));
    trialDataStudy.getTrialDataInterventions();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode trialDataNode = mapper.convertValue(trialData, ObjectNode.class);

    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(trialverseService.getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri3"))).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(namespaceId, outcomeUri, Arrays.asList("uri1", "uri3"));

    assertEquals(2, problem.getEntries().size());
  }

  private TrialData createMockTrialData() {
    Long studyId1 = 101L;
    Long studyId2 = 202L;
    Long drugId1 = 420L;
    Long drugId2 = 430L;
    Long drugId3 = 440L;
    Long drugId4 = 550L;
    Long armId1 = 555L;
    Long armId2 = 666L;
    Long armId3 = 777L;
    Long armId4 = 888L;
    Long armId5 = 999L;

    TrialDataIntervention trialDataIntervention1 = new TrialDataIntervention(drugId1, "uri1", studyId1);
    TrialDataIntervention trialDataIntervention2 = new TrialDataIntervention(drugId2, "uri2", studyId1);
    TrialDataIntervention trialDataIntervention3 = new TrialDataIntervention(drugId3, "uri3", studyId1);

    TrialDataIntervention trialDataIntervention4 = new TrialDataIntervention(drugId4, "uri3", studyId2);

    List<TrialDataIntervention> trialdataInterventions1 = Arrays.asList(trialDataIntervention1, trialDataIntervention2, trialDataIntervention3);
    List<TrialDataIntervention> trialdataInterventions2 = Arrays.asList(trialDataIntervention4);

    Measurement measurement1 = new Measurement(studyId1, 333L, 444L, armId1, MeasurementAttribute.SAMPLE_SIZE, 768784L, null);
    Measurement measurement2 = new Measurement(studyId1, 333L, 444L, armId2, MeasurementAttribute.STANDARD_DEVIATION, null, Math.E);
    Measurement measurement3 = new Measurement(studyId1, 333L, 444L, armId2, MeasurementAttribute.MEAN, null, Math.PI);

    Measurement measurement4 = new Measurement(studyId1, 333L, 444L, armId3, MeasurementAttribute.SAMPLE_SIZE, -1L, null);
    Measurement measurement5 = new Measurement(studyId1, 333L, 444L, armId3, MeasurementAttribute.RATE, -1L, null);

    Measurement measurement6 = new Measurement(studyId1, 333L, 444L, armId4, MeasurementAttribute.SAMPLE_SIZE, -1L, null);
    Measurement measurement7 = new Measurement(studyId1, 333L, 444L, armId4, MeasurementAttribute.RATE, -1L, null);

    Measurement measurement8 = new Measurement(studyId2, 333L, 444L, armId5, MeasurementAttribute.SAMPLE_SIZE, -1L, null);
    Measurement measurement9 = new Measurement(studyId2, 333L, 444L, armId5, MeasurementAttribute.RATE, -1L, null);

    List<Measurement> measurements1 = Arrays.asList(measurement1, measurement2, measurement3);
    List<Measurement> measurements2 = Arrays.asList(measurement1, measurement2, measurement3);
    List<Measurement> measurements3 = Arrays.asList(measurement4, measurement5);
    List<Measurement> measurements4 = Arrays.asList(measurement6, measurement7);

    List<Measurement> measurements5 = Arrays.asList(measurement8, measurement9);

    TrialDataArm trialDataArm1 = new TrialDataArm(armId1, studyId1, "arm bb", drugId1, measurements1);
    TrialDataArm trialDataArm2 = new TrialDataArm(armId2, studyId1, "arm aa", drugId2, measurements2);
    TrialDataArm trialDataArm3 = new TrialDataArm(armId3, studyId1, "aaa", drugId2, measurements3);
    TrialDataArm trialDataArm4 = new TrialDataArm(armId4, studyId1, "qqqq", drugId3, measurements4);

    TrialDataArm trialDataArm5 = new TrialDataArm(armId5, studyId2, "yyyy", drugId4, measurements5);

    List<TrialDataArm> trialDataArms1 = Arrays.asList(trialDataArm1, trialDataArm2, trialDataArm3, trialDataArm4);
    List<TrialDataArm> trialDataArms2 = Arrays.asList(trialDataArm5);
    TrialDataStudy trialDataStudy1 = new TrialDataStudy(1L, "study1", trialdataInterventions1, trialDataArms1);
    TrialDataStudy trialDataStudy2 = new TrialDataStudy(2L, "study2", trialdataInterventions2, trialDataArms2);

    List<TrialDataStudy> trialDataStudies = Arrays.asList(trialDataStudy1, trialDataStudy2);
    return new TrialData(trialDataStudies);
  }

}
