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
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
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
    List<String> outcomeUris = Arrays.asList(outcomeUri1, outcomeUri2);

    List<Long> drugIds = Arrays.asList(1001L, 1002L, 1003L);
    List<Long> outcomeIds = Arrays.asList(2001L, 2002L, 2003L);

    Problem exampleProblem = createExampleProblem(exampleAnalysis);
    Project project = mock(Project.class);
    when(project.getTrialverseId()).thenReturn(namespaceId);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(exampleAnalysis);
    when(triplestoreService.getTrialverseDrugIds(namespaceId, studyId, interventionUris)).thenReturn(drugIds);
    when(triplestoreService.getTrialverseOutcomeIds(namespaceId, studyId, outcomeUris)).thenReturn(outcomeIds);
    List<String> armNames = Arrays.asList("paroxetine 40 mg/day", "fluoxetine 20 mg/day");
    when(trialverseService.getArmNamesByDrugIds(studyId, drugIds)).thenReturn(armNames);
    ObjectMapper mapper = new ObjectMapper();

    Variable variable1 = new Variable(1L, 11L, "HAM-D Responders", "description 1", "my unit is...", true, MeasurementType.RATE, "Test2");
    Variable variable2 = new Variable(2L, 12L, "Insomnia", "description 2", "my unit is...", true, MeasurementType.CONTINUOUS, "Test");

    ObjectNode objectNode1 =  mapper.valueToTree(variable1);
    ObjectNode objectNode2 = mapper.valueToTree(variable2);
    List<ObjectNode> variables = Arrays.asList(objectNode1, objectNode2);
    when(trialverseService.getVariablesByOutcomeIds(outcomeIds)).thenReturn(variables);

    // Executor
    Problem actualProblem = problemService.getProblem(projectId, analysisId);

    List<AlternativeEntry> expectedAlternativeEntries = new ArrayList<>(exampleProblem.getAlternatives().values());
    List<AlternativeEntry> actualAlternativeEntries = new ArrayList<>(actualProblem.getAlternatives().values());
    List<CriterionEntry> expectedCriterionEntries = new ArrayList<>(exampleProblem.getCriteria().values());
    List<CriterionEntry> actualCriterionEntries = new ArrayList<>(actualProblem.getCriteria().values());
    assertArrayEquals(expectedAlternativeEntries.toArray(), actualAlternativeEntries.toArray());
    assertArrayEquals(expectedCriterionEntries.toArray(), actualCriterionEntries.toArray());
    assertEquals(exampleProblem, actualProblem);

    verify(analysisRepository).get(projectId, analysisId);
    verify(triplestoreService).getTrialverseDrugIds(namespaceId, studyId, interventionUris);
    verify(triplestoreService).getTrialverseOutcomeIds(namespaceId, studyId, outcomeUris);
    verify(trialverseService).getVariablesByOutcomeIds(outcomeIds);
    verify(trialverseService).getArmNamesByDrugIds(studyId, drugIds);
  }

  private Problem createExampleProblem(Analysis analysis) {
    String title = analysis.getName();
    String alternative1Key = "paroxetine-40-mgday";
    String alternative1Title = "paroxetine 40 mg/day";
    String alternative2Key = "fluoxetine-20-mgday";
    String alternative2Title = "fluoxetine 20 mg/day";

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    alternatives.put(alternative1Key, new AlternativeEntry(alternative1Title));
    alternatives.put(alternative2Key, new AlternativeEntry(alternative2Title));


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

    return new Problem(title, alternatives, criteria);
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
