package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.problems.service.model.ContinuousMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

;

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
  CovariateRepository covariateRepository;

  @Mock
  PerformanceTableBuilder performanceTablebuilder;

  @Mock
  InterventionRepository interventionRepository;

  @Mock
  ModelRepository modelRepository;

  @Mock
  OutcomeRepository outcomeRepository;

  @Mock
  PataviTaskRepository pataviTaskRepository;

  @Mock
  MappingService mappingService;

  @Mock
  TrialverseService trialverseService;
  @InjectMocks
  ProblemService problemService;
  @Mock
  private TriplestoreService triplestoreService;
  private String namespaceUid = "UID 1";
  private String versionedUuid = "versionedUuid";

  @Before
  public void setUp() throws URISyntaxException {
    problemService = new ProblemServiceImpl();
    MockitoAnnotations.initMocks(this);
    versionedUuid = "versionedUuid";
    when(mappingService.getVersionedUuid(namespaceUid)).thenReturn(versionedUuid);
  }

  @After
  public void cleanUp() throws URISyntaxException {
    verifyNoMoreInteractions(analysisRepository, projectRepository, singleStudyBenefitRiskAnalysisRepository,
            interventionRepository, trialverseService, triplestoreService, mappingService, modelRepository);
  }

  @Test
  public void testGetSingleStudyBenefitRiskProblem() throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException {
    int projectId = 1;
    String projectVersion = "projectVersion";
    Project project = new Project(projectId, new Account("username", "first", "lasr", "email"), "name", "desc", namespaceUid, projectVersion);


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
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(analysis.getTitle()).thenReturn("analysisName");

    Outcome outcome1 = mock(Outcome.class);
    Outcome outcome2 = mock(Outcome.class);
    when(outcome1.getSemanticOutcomeUri()).thenReturn(criterionUri1);
    when(outcome2.getSemanticOutcomeUri()).thenReturn(criterionUri2);
    List<Outcome> outcomes = Arrays.asList(outcome1, outcome2);
    when(analysis.getSelectedOutcomes()).thenReturn(outcomes);

    SimpleIntervention intervention1 = new SimpleIntervention(-1, projectId, "name,", "moti", alternativeUri1, "slabel1");
    SimpleIntervention intervention2 = new SimpleIntervention(-2, projectId, "name,", "moti", alternativeUri2, "slabel2");

    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2);
    List<InterventionInclusion> interventionInclusions = Arrays.asList(
            new InterventionInclusion(analysisId, -1),
            new InterventionInclusion(analysisId, -2)
    );
    when(analysis.getSelectedInterventions()).thenReturn(interventionInclusions);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(versionedUuid);

    when(analysis.getStudyGraphUid()).thenReturn(studyUid);
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
    verify(analysisRepository).get(analysisId);
    verify(triplestoreService).getSingleStudyMeasurements(versionedUuid, studyUid, projectVersion, outcomeUids, interventionUids);
    verify(performanceTablebuilder).build(measurementRows);
    verify(mappingService).getVersionedUuid(namespaceUid);
    verify(interventionRepository).query(project.getId());

    assertNotNull(actualProblem);
    assertNotNull(actualProblem.getTitle());
    assertEquals(analysis.getTitle(), actualProblem.getTitle());
    assertNotNull(actualProblem.getAlternatives());
    assertNotNull(actualProblem.getCriteria());

    Map<String, CriterionEntry> actualCriteria = actualProblem.getCriteria();
    assertTrue(actualCriteria.keySet().contains(criterionUri1));
  }

  @Test
  public void testGetNetworkMetaAnalysisProblem() throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException {
    String version = "version 1";
    Integer projectId = 2;
    Integer analysisId = 3;

    List<TrialDataStudy> trialDataStudies = createMockTrialData();

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticVariable(outcomeUri, "label3"));
    List<ArmExclusion> armExclusions = new ArrayList<>();
    List<InterventionInclusion> interventionInclusions = new ArrayList<>();
    List<CovariateInclusion> covariateInclusions = new ArrayList<>();
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", armExclusions, interventionInclusions, covariateInclusions, outcome);

    Set<ArmExclusion> exclusions = new HashSet<>();
    exclusions.add(new ArmExclusion(analysis.getId(), "888L"));
    analysis.updateArmExclusions(exclusions);

    Project project = mock(Project.class);
    when(project.getDatasetVersion()).thenReturn(version);

    Covariate covariate1 = Mockito.spy(new Covariate(projectId, "cov1", "covmov1", CovariateOption.ALLOCATION_RANDOMIZED.toString(), null));
    Covariate covariate2 = Mockito.spy(new Covariate(projectId, "cov2", "covmov2", CovariateOption.MULTI_CENTER_STUDY.toString(), null));
    when(covariate1.getId()).thenReturn(1);
    when(covariate2.getId()).thenReturn(2);
    Collection<Covariate> covariates = Arrays.asList(covariate1, covariate2);

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    int interventionId1 = 1;
    SimpleIntervention intervention1 = new SimpleIntervention(interventionId1, projectId, "int1", "moti", semanticIntervention1);
    SimpleIntervention intervention2 = new SimpleIntervention(2, projectId, "int2", "moti", semanticIntervention2);
    SimpleIntervention intervention3 = new SimpleIntervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysis.getId(), intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysis.getId(), intervention2.getId());
    InterventionInclusion interventionInclusion3 = new InterventionInclusion(analysis.getId(), intervention3.getId());
    analysis.updateIncludedInterventions(new HashSet<>(Arrays.asList(interventionInclusion1, interventionInclusion2, interventionInclusion3)));

    ObjectMapper mapper = new ObjectMapper();

    List<ObjectNode> trialDataNode = new ArrayList<>();
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      trialDataNode.add(mapper.convertValue(trialDataStudy, ObjectNode.class));
    }
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    when(trialverseService.getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"), Collections.EMPTY_LIST)).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri2", "uri3"), Collections.EMPTY_LIST);
    verify(mappingService).getVersionedUuid(namespaceUid);

    assertNotNull(problem);
    assertEquals(3, problem.getEntries().size());
    ContinuousNetworkMetaAnalysisProblemEntry entry = new ContinuousNetworkMetaAnalysisProblemEntry("study1", interventionId1, 768784L, Math.PI, Math.E);
    assertTrue(problem.getEntries().contains(entry));
  }

  @Test
  public void testGetNetworkAnalysisProblemWithInterventionInclusions() throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException {
    String version = "version 1";
    Integer projectId = 2;
    Integer analysisId = 3;

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticVariable(outcomeUri, "label3"));
    List<ArmExclusion> armExclusions = new ArrayList<>();
    List<InterventionInclusion> interventionInclusions = new ArrayList<>();
    List<CovariateInclusion> covariateInclusions = new ArrayList<>();

    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", armExclusions, interventionInclusions, covariateInclusions, outcome);

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    SimpleIntervention intervention1 = new SimpleIntervention(1, projectId, "int1", "moti", semanticIntervention1);
    SimpleIntervention intervention2 = new SimpleIntervention(2, projectId, "int2", "moti", semanticIntervention2);
    SimpleIntervention intervention3 = new SimpleIntervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(version);

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysis.getId(), intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysis.getId(), intervention3.getId());
    analysis.updateIncludedInterventions(new HashSet<>(Arrays.asList(interventionInclusion1, interventionInclusion2)));

    Covariate covariate1 = Mockito.spy(new Covariate(projectId, "cov1", "covmov1", CovariateOption.ALLOCATION_RANDOMIZED.toString(), null));
    Covariate covariate2 = Mockito.spy(new Covariate(projectId, "cov2", "covmov2", CovariateOption.MULTI_CENTER_STUDY.toString(), null));
    when(covariate1.getId()).thenReturn(1);
    when(covariate2.getId()).thenReturn(2);
    Collection<Covariate> covariates = Arrays.asList(covariate1, covariate2);

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
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    when(trialverseService.getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri3"), Collections.EMPTY_LIST)).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(interventionRepository).query(projectId);
    verify(trialverseService).getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri3"), Collections.EMPTY_LIST);
    verify(mappingService).getVersionedUuid(namespaceUid);

    assertEquals(2, problem.getEntries().size());
  }

  @Test
  public void testGetNetworkAnalysisProblemWithCovaraites() throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException {
    String version = "version 1";
    Integer projectId = 2;
    Integer analysisId = 3;

    String outcomeUri = "outcomeUri";
    Outcome outcome = new Outcome(1213, projectId, "outcome", "moti", new SemanticVariable(outcomeUri, "label3"));
    List<ArmExclusion> armExclusions = new ArrayList<>();
    List<InterventionInclusion> interventionInclusions = new ArrayList<>();
    List<CovariateInclusion> covariateInclusions = new ArrayList<>();

    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis", armExclusions, interventionInclusions, covariateInclusions, outcome);

    SemanticIntervention semanticIntervention1 = new SemanticIntervention("uri1", "label");
    SemanticIntervention semanticIntervention2 = new SemanticIntervention("uri2", "label2");
    SemanticIntervention semanticIntervention3 = new SemanticIntervention("uri3", "label3");
    SimpleIntervention intervention1 = new SimpleIntervention(1, projectId, "int1", "moti", semanticIntervention1);
    SimpleIntervention intervention2 = new SimpleIntervention(2, projectId, "int2", "moti", semanticIntervention2);
    SimpleIntervention intervention3 = new SimpleIntervention(3, projectId, "int3", "moti", semanticIntervention3);
    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2, intervention3);

    Covariate covariate1 = Mockito.spy(new Covariate(projectId, "cov1", "covmov1", CovariateOption.ALLOCATION_RANDOMIZED.toString(), null));
    Covariate covariate2 = Mockito.spy(new Covariate(projectId, "cov2", "covmov2", CovariateOption.MULTI_CENTER_STUDY.toString(), null));
    when(covariate1.getId()).thenReturn(1);
    when(covariate2.getId()).thenReturn(2);
    Collection<Covariate> covariates = Arrays.asList(covariate1, covariate2);

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(version);

    CovariateInclusion covariateInclusion1 = Mockito.spy(new CovariateInclusion(analysis.getId(), covariate1.getId()));
    CovariateInclusion covariateInclusion2 = Mockito.spy(new CovariateInclusion(analysis.getId(), covariate2.getId()));
    when(covariateInclusion1.getId()).thenReturn(101);
    when(covariateInclusion2.getId()).thenReturn(202);
    analysis.updateIncludedCovariates(new HashSet<>(Arrays.asList(covariateInclusion1, covariateInclusion2)));

    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysis.getId(), intervention1.getId());
    InterventionInclusion interventionInclusion2 = new InterventionInclusion(analysis.getId(), intervention3.getId());
    analysis.updateIncludedInterventions(new HashSet<>(Arrays.asList(interventionInclusion1, interventionInclusion2)));

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
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    List<String> includedCovariateKeys = Arrays.asList(covariate1.getDefinitionKey(), covariate2.getDefinitionKey());
    when(trialverseService.getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri3"), includedCovariateKeys)).thenReturn(trialDataNode);

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(interventionRepository).query(projectId);

    verify(trialverseService).getTrialData(versionedUuid, version, outcomeUri, Arrays.asList("uri1", "uri3"), includedCovariateKeys);
    verify(mappingService).getVersionedUuid(namespaceUid);

    assertEquals(2, problem.getEntries().size());
    assertEquals(2, problem.getStudyLevelCovariates().size());
    assertTrue(problem.getStudyLevelCovariates().get("study1").keySet().contains("cov1"));
  }

  @Test
  public void testGetMetaBRProblem() throws ResourceDoesNotExistException, SQLException, IOException, URISyntaxException, InvalidModelException, ReadValueException {

    String version = "version 1";
    Integer projectId = 1;
    Integer analysisId = 2;
    String title = "title";

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(version);

    Set<InterventionInclusion> includedAlternatives = new HashSet<>(3);
    SimpleIntervention intervention1 = new SimpleIntervention(11, projectId, "fluox", "", new SemanticIntervention("uri1", "fluoxS"));
    SimpleIntervention intervention2 = new SimpleIntervention(12, projectId, "parox", "", new SemanticIntervention("uri2", "paroxS"));
    SimpleIntervention intervention3 = new SimpleIntervention(13, projectId, "sertr", "", new SemanticIntervention("uri3", "sertrS"));
    includedAlternatives.addAll(Arrays.asList(new InterventionInclusion(analysisId, 11), new InterventionInclusion(analysisId, 12), new InterventionInclusion(analysisId, 13)));

    SimpleIntervention intervention4 = new SimpleIntervention(14, projectId, "foo", "", new SemanticIntervention("uri4", "fooS"));
    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2, intervention3, intervention4);

    PataviTask pataviTask1 = new PataviTask(41, "gemtc", "problem");
    PataviTask pataviTask2 = new PataviTask(42, "gemtc", "problem");
    List<PataviTask> pataviTasks = Arrays.asList(pataviTask1, pataviTask2);

    Model model1 = new Model.ModelBuilder(analysisId, "model 1")
            .id(71)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .taskId(pataviTask1.getId())
            .link(Model.LINK_IDENTITY)
            .build();
    Model model2 = new Model.ModelBuilder(analysisId, "model 2")
            .id(72)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .taskId(pataviTask2.getId())
            .link(Model.LINK_CLOGLOG)
            .build();
    List<Model> models = Arrays.asList(model1, model2);

    Outcome outcome1 = new Outcome(21, projectId, "ham", "", new SemanticVariable("outUri1", "hamS"));
    Outcome outcome2 = new Outcome(22, projectId, "headache", "", new SemanticVariable("outUri2", "headacheS"));
    List<Outcome> outcomes = Arrays.asList(outcome1, outcome2);


    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, title, includedAlternatives);
    Integer nma1Id = 31;
    Integer nma2Id = 32;
    String baseline1JsonString = "{\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";
    String baseline2JsonString ="{\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";;

    MbrOutcomeInclusion inclusion1 = new MbrOutcomeInclusion(analysisId, outcome1.getId(), nma1Id, model1.getId());
    inclusion1.setBaseline(baseline1JsonString);
    MbrOutcomeInclusion inclusion2 = new MbrOutcomeInclusion(analysisId, outcome2.getId(), nma2Id, model2.getId());
    inclusion2.setBaseline(baseline2JsonString);
    List<MbrOutcomeInclusion> outcomeInclusions = Arrays.asList(inclusion1, inclusion2);
    analysis.setMbrOutcomeInclusions(outcomeInclusions);

    ObjectMapper om = new ObjectMapper();
    String results1 = "{\n" +
            "  \"multivariateSummary\": {\n" +
            "    \"11\": {\n" +
            "      \"mu\": {\n" +
            "        \"d.11.12\": 0.55302,\n" +
            "        \"d.11.13\": 0.46622\n" +
            "      },\n" +
            "      \"sigma\": {\n" +
            "        \"d.11.12\": {\n" +
            "          \"d.11.12\": 74.346,\n" +
            "          \"d.11.13\": 1.9648\n" +
            "        },\n" +
            "        \"d.11.13\": {\n" +
            "          \"d.11.12\": 1.9648,\n" +
            "          \"d.11.13\": 74.837\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    Map<Integer, JsonNode> results = new HashMap<>();
    JsonNode task1Results = om.readTree(results1);
    JsonNode task2Results = om.readTree(results1);
    results.put(pataviTask1.getId(), task1Results);
    results.put(pataviTask2.getId(), task2Results);

    List<Integer> modelIds = Arrays.asList(model2.getId(), model1.getId());
    List<Integer> outcomeIds = Arrays.asList(outcome2.getId(), outcome1.getId());
    when(projectRepository.get(projectId)).thenReturn(project);
    when(modelRepository.get(modelIds)).thenReturn(models);
    when(outcomeRepository.get(projectId, outcomeIds)).thenReturn(outcomes);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    List<Integer> taskIds = Arrays.asList(model1.getTaskId(), model2.getTaskId());
    when(pataviTaskRepository.findByIds(taskIds)).thenReturn(pataviTasks);
    when(pataviTaskRepository.getResults(taskIds)).thenReturn(results);
    when(interventionRepository.query(projectId)).thenReturn(interventions);

    MetaBenefitRiskProblem problem = (MetaBenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(modelRepository).get(modelIds);
    verify(outcomeRepository).get(projectId, outcomeIds);
    verify(analysisRepository).get(analysisId);
    verify(pataviTaskRepository).findByIds(taskIds);
    verify(pataviTaskRepository).getResults(taskIds);
    verify(interventionRepository).query(projectId);

    assertEquals(3, problem.getAlternatives().size());
    assertEquals(2, problem.getCriteria().size());
    assertEquals(2, problem.getPerformanceTable().size());
    assertEquals("relative-cloglog-normal", problem.getPerformanceTable().get(0).getPerformance().getType());
    assertEquals("relative-normal", problem.getPerformanceTable().get(1).getPerformance().getType());
    List<List<Double>> expectedDataHeadache = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
    assertEquals(expectedDataHeadache ,problem.getPerformanceTable().get(0).getPerformance().getParameters().getRelative().getCov().getData());
    List<List<Double>> expectedDataHam = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
    assertEquals(expectedDataHam ,problem.getPerformanceTable().get(1).getPerformance().getParameters().getRelative().getCov().getData());
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

    trialDataStudy1.addCovariateValue(new CovariateStudyValue(studyId1, CovariateOption.ALLOCATION_RANDOMIZED.toString().toString(), 1D));
    trialDataStudy1.addCovariateValue(new CovariateStudyValue(studyId1, CovariateOption.MULTI_CENTER_STUDY.toString().toString(), 2D));

    trialDataStudy2.addCovariateValue(new CovariateStudyValue(studyId2, CovariateOption.ALLOCATION_RANDOMIZED.toString().toString(), null));
    trialDataStudy2.addCovariateValue(new CovariateStudyValue(studyId2, CovariateOption.MULTI_CENTER_STUDY.toString().toString(), null));

    return Arrays.asList(trialDataStudy1, trialDataStudy2);
  }

}
