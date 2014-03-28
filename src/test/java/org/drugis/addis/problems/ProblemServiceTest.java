package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TrialverseServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  TriplestoreService triplestoreService;

  @Mock
  TrialverseService trialverseService;

  @Mock
  ProjectRepository projectRepository;

  @InjectMocks
  ProblemService problemService;

  @Before
  public void setUp() {
    problemService = new ProblemServiceImpl();
    trialverseService = new TrialverseServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetProblem() throws ResourceDoesNotExistException {
    Analysis exampleAnalysis = createAnalysis();
    int namespaceId = 1;
    int projectId = exampleAnalysis.getProjectId();
    int analysisId = exampleAnalysis.getId();
    Integer studyId = exampleAnalysis.getStudyId();

    String interventionUri1 = exampleAnalysis.getSelectedInterventions().get(0).getSemanticInterventionUri();
    String interventionUri2 = exampleAnalysis.getSelectedInterventions().get(1).getSemanticInterventionUri();
    List<String> interventionUris = Arrays.asList(interventionUri1, interventionUri2);

    String outcomeUri1 = exampleAnalysis.getSelectedOutcomes().get(0).getSemanticOutcomeUri();
    String outcomeUri2 = exampleAnalysis.getSelectedOutcomes().get(1).getSemanticOutcomeUri();
    Map<String, Outcome> outcomesByUri = new HashMap<>();
    Outcome outcome1 = mock(Outcome.class);
    Outcome outcome2 = mock(Outcome.class);
    when(outcome1.getName()).thenReturn("outcome 1");
    when(outcome2.getName()).thenReturn("outcome 2");
    outcomesByUri.put(outcomeUri1, outcome1);
    outcomesByUri.put(outcomeUri2, outcome2);

    Map<Long, String> drugs = new HashMap<>();
    drugs.put(1001L, "uuid1");
    drugs.put(1002L, "uuid2");
    drugs.put(1003L, "uuid3");

    Map<Long, String> outcomes = new HashMap<>();
    drugs.put(2001L, "uuid1");
    drugs.put(2002L, "uuid2");
    drugs.put(2003L, "uuid3");

    Problem exampleProblem = createExampleProblem(exampleAnalysis);
    Project project = mock(Project.class);
    when(project.getTrialverseId()).thenReturn(namespaceId);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(exampleAnalysis);
    when(triplestoreService.getTrialverseDrugs(namespaceId, studyId, interventionUris)).thenReturn(drugs);
    when(triplestoreService.getTrialverseVariables(namespaceId, studyId, outcomesByUri.keySet())).thenReturn(outcomes);

    ObjectMapper mapper = new ObjectMapper();

    Arm arm1 = new Arm(1L, 1001L, "paroxetine 40 mg/day");
    Arm arm2 = new Arm(2L, 1002L, "fluoxetine 20 mg/day");
    ObjectNode armNode1 = mapper.valueToTree(arm1);
    ObjectNode armNode2 = mapper.valueToTree(arm2);
    List<ObjectNode> arms = Arrays.asList(armNode1, armNode2);
    List<Long> armIds = Arrays.asList(arm1.getId(), arm2.getId());
    when(trialverseService.getArmsByDrugIds(studyId, drugs.keySet())).thenReturn(arms);

    Variable variable1 = new Variable(1L, 11L, "HAM-D Responders", "description 1", "my unit is...", true, MeasurementType.RATE, "Test2");
    Variable variable2 = new Variable(2L, 12L, "Insomnia", "description 2", "my unit is...", true, MeasurementType.CONTINUOUS, "Test");

    ObjectNode variableNode1 =  mapper.valueToTree(variable1);
    ObjectNode variableNode2 = mapper.valueToTree(variable2);
    List<ObjectNode> variables = Arrays.asList(variableNode1, variableNode2);
    when(trialverseService.getVariablesByIds(outcomes.keySet())).thenReturn(variables);

    Long measurementMomentId = 1L;
    Measurement measurement1 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.RATE, 42L, null);
    Measurement measurement2 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 68L, null);
    Measurement measurement3 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.MEAN, null, 7.56);
    Measurement measurement4 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 44L, null);
    Measurement measurement5 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.STANDARD_DEVIATION, null, 2.1);

    ObjectNode measurementNode1 =  mapper.valueToTree(measurement1);
    ObjectNode measurementNode2 =  mapper.valueToTree(measurement2);
    ObjectNode measurementNode3 =  mapper.valueToTree(measurement3);
    ObjectNode measurementNode4 =  mapper.valueToTree(measurement4);
    ObjectNode measurementNode5 =  mapper.valueToTree(measurement5);

    List<ObjectNode> measurements = Arrays.asList(measurementNode1, measurementNode2, measurementNode3, measurementNode4, measurementNode5);
    when(trialverseService.getOrderedMeasurements(studyId, outcomes.keySet(), armIds)).thenReturn(measurements);

    // Executor
    Problem actualProblem = problemService.getProblem(projectId, analysisId);

    List<AlternativeEntry> expectedAlternativeEntries = new ArrayList<>(exampleProblem.getAlternatives().values());
    List<AlternativeEntry> actualAlternativeEntries = new ArrayList<>(actualProblem.getAlternatives().values());
    List<CriterionEntry> expectedCriterionEntries = new ArrayList<>(exampleProblem.getCriteria().values());
    List<CriterionEntry> actualCriterionEntries = new ArrayList<>(actualProblem.getCriteria().values());
    assertArrayEquals(expectedAlternativeEntries.toArray(), actualAlternativeEntries.toArray());
    assertArrayEquals(expectedCriterionEntries.toArray(), actualCriterionEntries.toArray());

    List<AbstractMeasurementEntry> expectedPerformance = exampleProblem.getPerformanceTable();
    List<AbstractMeasurementEntry> actualPerformance = actualProblem.getPerformanceTable();
    assertEquals(new HashSet<>(expectedPerformance), new HashSet<>(actualPerformance));

    verify(analysisRepository).get(projectId, analysisId);
    verify(triplestoreService).getTrialverseDrugs(namespaceId, studyId, interventionUris);
    verify(triplestoreService).getTrialverseVariables(namespaceId, studyId, outcomesByUri.keySet());
    verify(trialverseService).getVariablesByIds(outcomes.keySet());
    verify(trialverseService).getArmsByDrugIds(studyId, drugs.keySet());
    verify(trialverseService).getOrderedMeasurements(studyId, outcomes.keySet(), armIds);
  }

  private Problem createExampleProblem(Analysis analysis) {
    String title = analysis.getName();
    String alternative1Key = "paroxetine-40-mgday";
    String alternative1Title = "paroxetine 40 mg/day";
    String alternative2Key = "fluoxetine-20-mgday";
    String alternative2Title = "fluoxetine 20 mg/day";

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    AlternativeEntry alternativeEntry1 = new AlternativeEntry(alternative1Title);
    AlternativeEntry alternativeEntry2 = new AlternativeEntry(alternative2Title);
    alternatives.put(alternative1Key, alternativeEntry1);
    alternatives.put(alternative2Key, alternativeEntry2);


    String criterion1Key = "ham-d-responders";
    String criterion1Title = "HAM-D Responders";
    String criterion2Key = "insomnia";
    String criterion2Title = "Insomnia";

    Map<String, CriterionEntry> criteria = new HashMap<>();
    List<Double> nullScale = Arrays.asList(null, null);
    CriterionEntry criterionEntry1 = new CriterionEntry(criterion1Title, Arrays.asList(0.0, 1.0), null);
    CriterionEntry criterionEntry2 = new CriterionEntry(criterion2Title, nullScale, null);
    criteria.put(criterion1Key, criterionEntry1);
    criteria.put(criterion2Key, criterionEntry2);

    Arm arm1 = new Arm(1L, 1001L, "paroxetine 40 mg/day");
    Arm arm2 = new Arm(2L, 1002L, "fluoxetine 20 mg/day");

    Variable variable1 = new Variable(101L, 1L, "HAM-D Responders", "desc", null, false, MeasurementType.RATE, "");
    Variable variable2 = new Variable(102L, 1L, "Insomnia", "desc", null, false, MeasurementType.CONTINUOUS, "");

    Long measurementMomentId = 1L;

    Measurement measurement1 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.RATE, 42L, null);
    Measurement measurement2 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 68L, null);
    Measurement measurement3 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.MEAN, null, 7.56);
    Measurement measurement4 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 44L, null);
    Measurement measurement5 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.STANDARD_DEVIATION, null, 2.1);

    List<Measurement> measurements = Arrays.asList(measurement1, measurement2, measurement3, measurement4, measurement5);

    Map<Long, CriterionEntry> criteriaCache = new HashMap<>();
    criteriaCache.put(variable1.getId(), criterionEntry1);
    criteriaCache.put(variable2.getId(), criterionEntry2);


    Map<Long, AlternativeEntry> alternativesCache= new HashMap<>();
    alternativesCache.put(arm1.getId(), alternativeEntry1);
    alternativesCache.put(arm2.getId(), alternativeEntry2);
    PerformanceTableBuilder builder = new PerformanceTableBuilder(criteriaCache, alternativesCache, measurements);

    return new Problem(title, alternatives, criteria, builder.build());
  }

  private Analysis createAnalysis() {
    Outcome outcome1 = new Outcome(1, 1, "outcome1", "motivation", new SemanticOutcome("oUri1", "label"));
    Outcome outcome2 = new Outcome(2, 1, "outcome2", "motivation", new SemanticOutcome("oUri2", "label"));
    List<Outcome> outcomes = Arrays.asList(outcome1, outcome2);
    Intervention intervention1 = new Intervention(1, 1, "intervention1", "motivation", new SemanticIntervention("iUri1", "label"));
    Intervention intervention2 = new Intervention(2, 1, "intervention2", "motivation", new SemanticIntervention("iUri2", "label"));
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2);
    Analysis analysis = new Analysis(1, 1, "analysisName", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, outcomes, interventions);
    analysis.setStudyId(5);
    return analysis;
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(analysisRepository, triplestoreService, trialverseService);
  }
}
