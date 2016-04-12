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
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
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
  ModelRepository modelRepository;

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
  public void testUpdateWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException {
    Integer analysisId = -6;
    Account user = mock(Account.class);
    Integer wrongProject = 2;
    Outcome outcome = mock(Outcome.class);
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, wrongProject, "new name", outcome);
    NetworkMetaAnalysis oldAnalysis = mock(NetworkMetaAnalysis.class);

    when(modelRepository.findByAnalysis(analysis.getId())).thenReturn(new ArrayList<Model>());
    when(oldAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(analysisId)).thenReturn(oldAnalysis);

    analysisService.updateNetworkMetaAnalysis(user, analysis);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateLockedAnalysisFails() throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelException, SQLException  {
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
    when(modelRepository.findByAnalysis(analysis.getId())).thenReturn(models);
    when(modelRepository.find(modelId)).thenReturn(null);

    analysisService.updateNetworkMetaAnalysis(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateWithOutcomeInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException {
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
  public void testBuildInitialOutcomeInclusionsCheckNmaNoOutcome() throws SQLException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Collection<Outcome> outcomes = Arrays.asList(new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable("uri", "label")));
    List<NetworkMetaAnalysis> analyses = Arrays.asList(new NetworkMetaAnalysis(analysisId, "title"));

    when(outcomeRepository.query(projectId)).thenReturn(outcomes);
    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildInitialOutcomeInclusionsWithoutPrimary() throws InvalidModelException, SQLException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Integer modelId1 = 1;
    Integer modelId2 = 2;
    Outcome outcome = new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable("uri", "label"));
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
    when(modelRepository.findNetworkModelsByProject(projectId)).thenReturn(models);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertEquals(Arrays.asList(new MbrOutcomeInclusion(metabenefitRiskAnalysisId, 1, analysisId, modelId2)), result);
  }

  @Test
  public void testBuildInitialOutcomeInclusionsWithPrimary() throws InvalidModelException , SQLException {
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Integer modelId1 = 1;
    Integer modelId2 = 2;
    Outcome outcome = new Outcome(outcomeId, 1, "name", "moti", new SemanticVariable("uri", "label"));
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
    when(modelRepository.findNetworkModelsByProject(projectId)).thenReturn(models);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertEquals(Arrays.asList(new MbrOutcomeInclusion(metabenefitRiskAnalysisId, 1, analysisId, modelId1)), result);
  }

  @Test
  public void buildEvidenceTable() throws ResourceDoesNotExistException, ReadValueException {

    List<ArmExclusion> excludedArms = Collections.emptyList();
    int includedInterventionId = 101;
    int sirNotAppearingInThisFilmId = 102;
    InterventionInclusion interventionInclusion1 = new InterventionInclusion(analysisId, includedInterventionId);
    List<InterventionInclusion> includedInterventions = Collections.singletonList(interventionInclusion1);
    List<CovariateInclusion> includedCovariates = Collections.emptyList();
    Outcome outcome = new Outcome(1, projectId, "outcome", null, new SemanticVariable("http://test/uri", "semantic outcome label"));
    AbstractAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(1, projectId, "title", excludedArms, includedInterventions, includedCovariates, outcome);
    String version = "version";
    Account owner = mock(Account.class);
    String namespaceUid = "namespaceUid";
    Project project = new Project(projectId, owner, "proj", "desc", namespaceUid, version);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(analysisId)).thenReturn(networkMetaAnalysis);
    AbstractIntervention intervention1 = new SimpleIntervention(includedInterventionId, projectId, "intervention1", "", new SemanticIntervention(URI.create("semUri1"), "intervention 1"));
    AbstractIntervention intervention2 = new SimpleIntervention(sirNotAppearingInThisFilmId, projectId, "intervention2", "", new SemanticIntervention(URI.create("semUri2"), "intervention 2"));
    List<AbstractIntervention> interventions = Arrays.asList(intervention1, intervention2);
    List<Covariate> covariates = Collections.emptyList();
    when(interventionRepository.query(projectId)).thenReturn(interventions);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    List<URI> includedInterventionUids = Collections.singletonList(intervention1.getSemanticInterventionUri());
    List<String> includedCovariateUids = Collections.emptyList();
    Measurement arm1Measurement = null;
    Measurement arm2Measurement = null;
    URI drugInstance1 = URI.create("foo/druginstance1");
    URI drugConcept1 = intervention1.getSemanticInterventionUri();
    URI drugInstance2 = URI.create("foo/druginstance2");
    URI drugConcept2 = intervention2.getSemanticInterventionUri();
    Dose minDose1 = new Dose(0.5, "P1D", URI.create("unitConceptUri"), "milligram", 0.001);
    Dose maxDose1 = new Dose(1.0, "P1D", URI.create("unitConceptUri"), "milligram", 0.001);
    AbstractSemanticIntervention arm1Intervention = new TitratedSemanticIntervention(drugInstance1, drugConcept1, minDose1, maxDose1);
    AbstractSemanticIntervention arm2Intervention = new SimpleSemanticIntervention(drugInstance2, drugConcept2);
    TrialDataArm arm1 = new TrialDataArm(URI.create("foo/armuri1"), "armname1", drugInstance1, arm1Measurement, arm1Intervention);
    TrialDataArm arm2 = new TrialDataArm(URI.create("foo/armuri2"), "armname2", drugInstance2, arm2Measurement, arm2Intervention);
    List<TrialDataArm> study1Arms = Arrays.asList(arm1, arm2);
    TrialDataStudy study1 = new TrialDataStudy(URI.create("studyUri"), "name", study1Arms);
    List<TrialDataStudy> trialData = Arrays.asList(study1);
    when(interventionService.isMatched(intervention1, arm1)).thenReturn(true);
    when(triplestoreService.getTrialData(project.getNamespaceUid(), project.getDatasetVersion(), outcome.getSemanticOutcomeUri(), includedInterventionUids, includedCovariateUids))
            .thenReturn(trialData);

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(projectId, analysisId);

    verify(triplestoreService).getTrialData(project.getNamespaceUid(), project.getDatasetVersion(), outcome.getSemanticOutcomeUri(), includedInterventionUids, includedCovariateUids);

    assertNotNull(trialDataStudies);
    assertEquals(1, trialDataStudies.size());
    assertEquals((Integer) includedInterventionId, trialDataStudies.get(0).getTrialDataArms().get(0).getMatchedProjectInterventionId());
  }

}