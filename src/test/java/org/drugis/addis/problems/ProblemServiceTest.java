package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.ArmExclusion;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
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
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
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
  PerformanceTableBuilder performanceTablebuilder;

  @Mock
  InterventionRepository interventionRepository;

  @Mock
  TrialverseService trialverseService;

  @Mock
  private TriplestoreService triplestoreService;

  @InjectMocks
  ProblemService problemService;

  @Before
  public void setUp() {
    problemService = new ProblemServiceImpl();
    MockitoAnnotations.initMocks(this);

  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(analysisRepository, projectRepository, singleStudyBenefitRiskAnalysisRepository,
            interventionRepository, trialverseService, triplestoreService);
  }

  @Test
  public void testGetSingleStudyBenefitRiskProblem() throws ResourceDoesNotExistException {

    int projectId = 1;
    String namespaceUid = "namespaceUid";
    String projectVersion = "projectVersion";
    Project project = mock(Project.class);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(projectVersion);
    when(projectRepository.get(projectId)).thenReturn(project);

    int analysisId = 2;
    String studyUid = "3g0yg-g945gh";
    String criterionUri1 = "c1";
    String variableName1 = "vn1";
    String alternativeUri1 = "a1";
    String armName1 = "an1";

    String criterionUri2 = "c2";
    String variableName2 = "vn2";
    String alternativeUri2 = "a2";
    String armName2 = "an2";

    SingleStudyBenefitRiskAnalysis analysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(analysis.getName()).thenReturn("analysisName");

    Outcome outcome1 = mock(Outcome.class);
    Outcome outcome2 = mock(Outcome.class);
    when(outcome1.getSemanticOutcomeUri()).thenReturn(criterionUri1);
    when(outcome2.getSemanticOutcomeUri()).thenReturn(criterionUri2);
    List<Outcome> outcomes = Arrays.asList(outcome1, outcome2);
    when(analysis.getSelectedOutcomes()).thenReturn(outcomes);

    Intervention intervention1 = mock(Intervention.class);
    Intervention intervention2 = mock(Intervention.class);
    when(intervention1.getSemanticInterventionUri()).thenReturn(alternativeUri1);
    when(intervention2.getSemanticInterventionUri()).thenReturn(alternativeUri2);
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2);
    when(analysis.getSelectedInterventions()).thenReturn(interventions);

    when(analysis.getStudyUid()).thenReturn(studyUid);
    List<String> outcomeUids = Arrays.asList(criterionUri1, criterionUri2);
    List<String> interventionUids = Arrays.asList(alternativeUri1, alternativeUri2);

    Long rate = 42L;
    Long sampleSize1 = 111L;

    Long sampleSize2 = 222L;
    Double mu = 7.56;
    Double stdDev = 0.2;
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row1 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri1, variableName1, alternativeUri1, armName1, mu, stdDev, null, sampleSize1);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row2 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri2, variableName2, alternativeUri2, armName2, null, null, rate, sampleSize2);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row3 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri1, variableName1, alternativeUri2, armName2, mu, stdDev, null, sampleSize1);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row4 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri2, variableName2, alternativeUri1, armName1, null, null, rate, sampleSize2);

    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> measurementRows = Arrays.asList(row1, row2, row3, row4);

    when(triplestoreService.getSingleStudyMeasurements(anyString(), anyString(), anyString(), anyList(), anyList())).thenReturn(measurementRows);

    AbstractMeasurementEntry measurementEntry = mock(ContinuousMeasurementEntry.class);
    List<AbstractMeasurementEntry> performanceTable = Arrays.asList(measurementEntry);
    when(performanceTablebuilder.build(measurementRows)).thenReturn(performanceTable);

    // execute
    SingleStudyBenefitRiskProblem actualProblem = (SingleStudyBenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(triplestoreService).getSingleStudyMeasurements(namespaceUid, studyUid, projectVersion, outcomeUids, interventionUids);
    verify(performanceTablebuilder).build(measurementRows);

    assertNotNull(actualProblem);
    assertNotNull(actualProblem.getTitle());
    assertEquals(analysis.getName(), actualProblem.getTitle());
    assertNotNull(actualProblem.getAlternatives());
    assertNotNull(actualProblem.getCriteria());

    Map<String, CriterionEntry> actualCriteria = actualProblem.getCriteria();
    assertTrue(actualCriteria.keySet().contains(criterionUri1));
  }

  @Test
  public void testGetNetworkMetaAnalysisProblem() throws ResourceDoesNotExistException {
    String namespaceUid = "UID 1";
    String version = "version 1";
    Integer projectId = 2;
    Integer analysisId = 3;

    List<TrialDataStudy> trialDataStudies = createMockTrialData();

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticOutcome(outcomeUri, "label3"));
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", new ArrayList<ArmExclusion>(), new ArrayList<InterventionInclusion>(), outcome);

    analysis.getExcludedArms().add(new ArmExclusion(analysis, "888L")); // trialDataArm with armId4

    Project project = mock(Project.class);
    when(project.getDatasetVersion()).thenReturn(version);

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    int interventionId1 = 1;
    Intervention intervention1 = new Intervention(interventionId1, projectId, "int1", "moti", semanticIntervention1);
    Intervention intervention2 = new Intervention(2, projectId, "int2", "moti", semanticIntervention2);
    Intervention intervention3 = new Intervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysis, intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysis, intervention2.getId());
    InterventionInclusion interventionInclusion3 = new InterventionInclusion(analysis, intervention3.getId());
    analysis.getIncludedInterventions().addAll(Arrays.asList(interventionInclusion1, interventionInclusion2, interventionInclusion3));

    ObjectMapper mapper = new ObjectMapper();

    List<ObjectNode> trialDataNode = new ArrayList<>();
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      trialDataNode.add(mapper.convertValue(trialDataStudy, ObjectNode.class));
    }
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(trialverseService.getTrialData(namespaceUid, version, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"))).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(namespaceUid, version, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"));

    assertNotNull(problem);
    assertEquals(3, problem.getEntries().size());
    ContinuousNetworkMetaAnalysisProblemEntry entry = new ContinuousNetworkMetaAnalysisProblemEntry("study1", interventionId1, 768784L, Math.PI, Math.E);
    assertTrue(problem.getEntries().contains(entry));
  }

  @Test
  public void testGetNetworkAnalysisProblemWithInterventionInclusions() throws ResourceDoesNotExistException {
    String namespaceUid = "UID 1";
    String version = "version 1";
    Integer projectId = 2;
    Integer analysisId = 3;

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticOutcome(outcomeUri, "label3"));
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", Collections.EMPTY_LIST, new ArrayList<InterventionInclusion>(), outcome);

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    Intervention intervention1 = new Intervention(1, projectId, "int1", "moti", semanticIntervention1);
    Intervention intervention2 = new Intervention(2, projectId, "int2", "moti", semanticIntervention2);
    Intervention intervention3 = new Intervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<Intervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(version);

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysis, intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysis, intervention3.getId());
    analysis.getIncludedInterventions().addAll(Arrays.asList(interventionInclusion1, interventionInclusion2));

    List<TrialDataStudy> trialDataStudies = createMockTrialData();
    TrialDataStudy firstTrialDataStudy = trialDataStudies.get(0);
    List<TrialDataIntervention> trialDataInterventions = firstTrialDataStudy.getTrialDataInterventions();
    // remove excluded intervention from trialdata as well (HACKY)
    trialDataInterventions.set(1, new TrialDataIntervention("-666L", "iamnothere", "-666L"));

    ObjectMapper mapper = new ObjectMapper();
    List<ObjectNode> trialDataNode = new ArrayList<>();
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      trialDataNode.add(mapper.convertValue(trialDataStudy, ObjectNode.class));
    }
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(trialverseService.getTrialData(namespaceUid, version, outcomeUri, Arrays.asList("uri1", "uri3"))).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(projectId, analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(namespaceUid, version, outcomeUri, Arrays.asList("uri1", "uri3"));

    assertEquals(2, problem.getEntries().size());
  }

  private List<TrialDataStudy> createMockTrialData() {
    String studyId1 = "101L";
    String studyId2 = "202L";
    String drugId1 = "420L";
    String drugId2 = "430L";
    String drugId3 = "440L";
    String drugId4 = "550L";
    String armId1 = "555L";
    String armId2 = "666L";
    String armId3 = "777L";
    String armId4 = "888L";
    String armId5 = "999L";

    String drugUid1 = "uri1";
    String drugUid2 = "uri2";
    String drugUid3 = "uri3";
    TrialDataIntervention trialDataIntervention1 = new TrialDataIntervention(drugId1, drugUid1, studyId1);
    TrialDataIntervention trialDataIntervention2 = new TrialDataIntervention(drugId2, drugUid2, studyId1);
    TrialDataIntervention trialDataIntervention3 = new TrialDataIntervention(drugId3, drugUid3, studyId1);

    TrialDataIntervention trialDataIntervention4 = new TrialDataIntervention(drugId4, "uri3", studyId2);

    List<TrialDataIntervention> trialdataInterventions1 = Arrays.asList(trialDataIntervention1, trialDataIntervention2, trialDataIntervention3);
    List<TrialDataIntervention> trialdataInterventions2 = Arrays.asList(trialDataIntervention4);

    Measurement measurement1 = new Measurement(studyId1, "333L", armId1, 768784L, null, Math.E, Math.PI);

    Measurement measurement2 = new Measurement(studyId1, "333L", armId3, -1L, -1L, null, null);
    Measurement measurement3 = new Measurement(studyId1, "333L", armId4, -1L, -1L, null, null);
    Measurement measurement4 = new Measurement(studyId1, "333L", armId5, -1L, -1L, null, null);
    Measurement measurement5 = new Measurement(studyId1, "333L", armId2, -1L, -1L, null, null);

    TrialDataArm trialDataArm1 = new TrialDataArm(armId1, "name1", studyId1, drugId1, drugUid1, measurement1);
    TrialDataArm trialDataArm2 = new TrialDataArm(armId2, "arm aa", studyId1, drugId2, drugUid2, measurement2);
    TrialDataArm trialDataArm3 = new TrialDataArm(armId3, "aaa", studyId1, drugId2, drugUid2, measurement3);
    TrialDataArm trialDataArm4 = new TrialDataArm(armId4, "qqqq", studyId1, drugId3, drugUid3, measurement4);
    TrialDataArm trialDataArm5 = new TrialDataArm(armId5, "yyyy", studyId2, drugId4, drugUid2, measurement5);

    List<TrialDataArm> trialDataArms1 = Arrays.asList(trialDataArm1, trialDataArm2, trialDataArm3, trialDataArm4);
    List<TrialDataArm> trialDataArms2 = Arrays.asList(trialDataArm5);
    TrialDataStudy trialDataStudy1 = new TrialDataStudy("1L", "study1", trialdataInterventions1, trialDataArms1);
    TrialDataStudy trialDataStudy2 = new TrialDataStudy("2L", "study2", trialdataInterventions2, trialDataArms2);

    return Arrays.asList(trialDataStudy1, trialDataStudy2);
  }

}
