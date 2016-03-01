package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.impl.AnalysisServiceImpl;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnalysisServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  ProjectService projectService;

  @Mock
  ModelRepository modelRepository;

  @Mock
  OutcomeRepository outcomeRepository;

  @Mock
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

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
  public void testUpdateLockedAnalysisFails() throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelTypeException, SQLException, InvalidHeterogeneityTypeException {
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
  public void testBuildInitialOutcomeInclusionsCheckNmaNoOutcome(){
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;

    Collection<Outcome> outcomes = Arrays.asList(new Outcome(outcomeId, 1, "name", "moti", new SemanticOutcome("uri", "label")));
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);
    List<NetworkMetaAnalysis> analyses = Arrays.asList(new NetworkMetaAnalysis(analysisId, "title"));
    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);
    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildInitialOutcomeInclusions(){
    Integer projectId = 1;
    Integer metabenefitRiskAnalysisId = 1;
    Integer outcomeId = 1;
    Outcome outcome = new Outcome(outcomeId, 1, "name", "moti", new SemanticOutcome("uri", "label"));
    Collection<Outcome> outcomes = Arrays.asList(outcome);
    List<NetworkMetaAnalysis> analyses = Arrays.asList(new NetworkMetaAnalysis(analysisId, projectId, "tittle", outcome));

    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, Arrays.asList(1))).thenReturn(analyses);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);

    List<MbrOutcomeInclusion> result = analysisService.buildInitialOutcomeInclusions(projectId, metabenefitRiskAnalysisId);

    assertEquals(Arrays.asList(new MbrOutcomeInclusion(metabenefitRiskAnalysisId, 1, analysisId)), result);
  }


}