package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.impl.AnalysisServiceImpl;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnalysisServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  ProjectService projectService;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  ModelService modelService;

  @Mock
  OutcomeRepository outcomeRepository;

  @Mock
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Mock
  private InterventionRepository interventionRepository;

  @Mock
  private CovariateRepository covariateRepository;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private InterventionService interventionService;

  @Mock
  private MappingService mappingService;

  @InjectMocks
  private AnalysisService analysisService;
  private Integer projectId = 3;
  private Integer analysisId = 2;

  @Before
  public void setUp() {
    analysisRepository = mock(AnalysisRepository.class);
    analysisService = new AnalysisServiceImpl();
    initMocks(this);
  }

  @Test
  public void testCheckCoordinatesSSBR() throws ResourceDoesNotExistException {
    SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(singleStudyBenefitRiskAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(analysisId)).thenReturn(singleStudyBenefitRiskAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);

    verify(analysisRepository).get(analysisId);
    verifyNoMoreInteractions(analysisRepository);
  }

  @Test
  public void testCheckCoordinatesNMA() throws ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = mock(NetworkMetaAnalysis.class);
    when(networkMetaAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(analysisId)).thenReturn(networkMetaAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);

    verify(analysisRepository).get(analysisId);
    verifyNoMoreInteractions(analysisRepository);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCheckAnalysisNotInProject() throws ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = mock(NetworkMetaAnalysis.class);
    when(analysisRepository.get(analysisId)).thenReturn(networkMetaAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    Integer analysisId = -6;
    Account user = mock(Account.class);
    Integer wrongProject = 2;
    Outcome outcome = mock(Outcome.class);
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, wrongProject, "new name", outcome);
    NetworkMetaAnalysis oldAnalysis = mock(NetworkMetaAnalysis.class);

    when(modelService.findByAnalysis(analysis.getId())).thenReturn(new ArrayList<Model>());
    when(oldAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(analysisId)).thenReturn(oldAnalysis);

    analysisService.updateNetworkMetaAnalysis(user, analysis);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateLockedAnalysisFails() throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelException, SQLException, IOException {
    Integer analysisId = -6;
    Account user = mock(Account.class);
    Integer projectId = 1;
    Outcome outcome = mock(Outcome.class);
    Integer modelId = 83473458;

    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "new name", outcome);
    String modelTitle = "modelTitle";
    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .id(-10)
            .linearModel("fixedModel")
            .modelType(Model.NETWORK_MODEL_TYPE)
            .link(Model.LINK_IDENTITY)
            .burnInIterations(5000)
            .inferenceIterations(20000)
            .thinningFactor(10)
            .build();
    List<Model> models = Collections.singletonList(model);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(modelService.findByAnalysis(analysis.getId())).thenReturn(models);
    when(modelService.find(modelId)).thenReturn(null);

    analysisService.updateNetworkMetaAnalysis(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateWithOutcomeInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    Account user = mock(Account.class);
    Outcome outcome = mock(Outcome.class);
    when(outcome.getProject()).thenReturn(projectId + 1);
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "new name", outcome);
    AbstractAnalysis oldAnalysis = mock(NetworkMetaAnalysis.class);
    when(oldAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(analysisId)).thenReturn(oldAnalysis);
    analysisService.updateNetworkMetaAnalysis(user, analysis);
  }

  @Test
  public void testBuildInitialOutcomeInclusionsCheckNmaNoOutcome() throws SQLException, IOException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Collection<Outcome> outcomes = Arrays.asList(new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable(URI.create("uri"), "label")));
    List<NetworkMetaAnalysis> analyses = Arrays.asList(new NetworkMetaAnalysis(analysisId, "title"));

    when(outcomeRepository.query(projectId)).thenReturn(outcomes);
    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildInitialOutcomeInclusionsWithoutPrimary() throws InvalidModelException, SQLException, IOException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Integer modelId1 = 1;
    Integer modelId2 = 2;
    Outcome outcome = new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable(URI.create("uri"), "label"));
    Collection<Outcome> outcomes = Arrays.asList(outcome);
    String title1 = "bbbbb";
    String title2 = "aaaaa";
    List<NetworkMetaAnalysis> analyses = Arrays.asList(new NetworkMetaAnalysis(analysisId, projectId, title1, outcome),
            new NetworkMetaAnalysis(4, projectId, title1, outcome));
    List<Model> models = Arrays.asList(new Model.ModelBuilder(analysisId, title1).id(modelId1).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build(),
            new Model.ModelBuilder(analysisId, title2).id(modelId2).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build(),
            new Model.ModelBuilder(-23, title2).id(modelId2).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build());

    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);
    when(modelService.findNetworkModelsByProject(projectId)).thenReturn(models);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertEquals(Arrays.asList(new MbrOutcomeInclusion(metabenefitRiskAnalysisId, 1, analysisId, modelId2)), result);
  }

  @Test
  public void testBuildInitialOutcomeInclusionsWithPrimary() throws InvalidModelException, SQLException, IOException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Integer modelId1 = 1;
    Integer modelId2 = 2;
    Outcome outcome = new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable(URI.create("uri"), "label"));
    Collection<Outcome> outcomes = Arrays.asList(outcome);
    String title1 = "bbbbb";
    String title2 = "aaaaa";
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisId, projectId, title1, outcome);
    networkMetaAnalysis.setPrimaryModel(modelId1);
    List<NetworkMetaAnalysis> analyses = Arrays.asList(networkMetaAnalysis,
            new NetworkMetaAnalysis(4, projectId, title1, outcome));
    List<Model> models = Arrays.asList(new Model.ModelBuilder(analysisId, title1).id(modelId1).link(Model.LINK_IDENTITY).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build(),
            new Model.ModelBuilder(3, title2).id(modelId2).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build());

    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);
    when(modelService.findNetworkModelsByProject(projectId)).thenReturn(models);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertEquals(Arrays.asList(new MbrOutcomeInclusion(metabenefitRiskAnalysisId, 1, analysisId, modelId1)), result);
  }

  @Test
  public void buildEvidenceTableTest() throws ResourceDoesNotExistException, ReadValueException, InvalidTypeForDoseCheckException, URISyntaxException {

    List<ArmExclusion> excludedArms = Collections.emptyList();
    int includedInterventionId = 101;
    int sirNotAppearingInThisFilmId = 102;
    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysisId, includedInterventionId);
    List<InterventionInclusion> includedInterventions = Collections.singletonList(interventionInclusion1);
    List<CovariateInclusion> includedCovariates = Collections.emptyList();
    Outcome outcome = new Outcome(1, projectId, "outcome", null, new SemanticVariable(URI.create("http://test/uri"), "semantic outcome label"));
    AbstractAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(1, projectId, "title", excludedArms, includedInterventions, includedCovariates, outcome);
    String version = "version";
    Account owner = mock(Account.class);
    String namespaceUid = "namespaceUid";
    Project project = new Project(projectId, owner, "proj", "desc", namespaceUid, version);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(analysisId)).thenReturn(networkMetaAnalysis);
    //todo WHAT ABOUT Combined interventions
    SingleIntervention includedIntervention = new SimpleIntervention(includedInterventionId, projectId, "includedIntervention", "", new SemanticInterventionUriAndName(URI.create("semUri1"), "intervention 1"));
    SingleIntervention notIncludedIntervention = new SimpleIntervention(sirNotAppearingInThisFilmId, projectId, "notIncludedIntervention", "", new SemanticInterventionUriAndName(URI.create("semUri2"), "intervention 2"));
    List<AbstractIntervention> interventions = Arrays.asList(includedIntervention, notIncludedIntervention);
    List<Covariate> covariates = Collections.emptyList();
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    Set<URI> includedInterventionUids = Collections.singleton(includedIntervention.getSemanticInterventionUri());
    Set<String> includedCovariateUids = Collections.emptySet();

    URI drugInstance1 = URI.create("foo/druginstance1");
    URI drugConcept1 = includedIntervention.getSemanticInterventionUri();
    URI drugInstance2 = URI.create("foo/druginstance2");
    URI drugConcept2 = notIncludedIntervention.getSemanticInterventionUri();
    Dose minDose1 = new Dose(0.5, "P1D", URI.create("unitConceptUri"), "milligram", 0.001);
    Dose maxDose1 = new Dose(1.0, "P1D", URI.create("unitConceptUri"), "milligram", 0.001);
    AbstractSemanticIntervention arm1Intervention = new TitratedSemanticIntervention(drugInstance1, drugConcept1, minDose1, maxDose1);
    AbstractSemanticIntervention arm2Intervention = new SimpleSemanticIntervention(drugInstance2, drugConcept2);
    TrialDataArm arm1 = new TrialDataArm(URI.create("foo/armuri1"), "armname1", drugInstance1, arm1Intervention);
    arm1.setMatchedProjectInterventionIds(new HashSet<>(Collections.singletonList(includedIntervention.getId())));
    TrialDataArm arm2 = new TrialDataArm(URI.create("foo/armuri2"), "armname2", drugInstance2, arm2Intervention);
    List<TrialDataArm> study1Arms = Arrays.asList(arm1, arm2);
    TrialDataStudy study1 = new TrialDataStudy(URI.create("studyUri"), "name", study1Arms);
    List<TrialDataStudy> trialData = Collections.singletonList(study1);
    when(triplestoreService.addMatchingInformation(Collections.singletonList(includedIntervention), trialData)).thenReturn(trialData);
    when(triplestoreService.getNetworkData(project.getNamespaceUid(), project.getDatasetVersion(), outcome.getSemanticOutcomeUri(), includedInterventionUids, includedCovariateUids))
            .thenReturn(trialData);
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(project.getNamespaceUid());

    //EXEC
    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(projectId, analysisId);

    verify(triplestoreService).getNetworkData(project.getNamespaceUid(), project.getDatasetVersion(), outcome.getSemanticOutcomeUri(), includedInterventionUids, includedCovariateUids);

    assertNotNull(trialDataStudies);
    assertEquals(1, trialDataStudies.size());
    assertTrue(trialDataStudies.get(0).getTrialDataArms().get(0).getMatchedProjectInterventionIds().contains(includedInterventionId));
  }

}