package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.patavitask.service.impl.PataviTaskServiceImpl;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

;

public class PataviTaskServiceTest {
  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private ProblemService problemService;

  @Mock
  private ModelService modelService;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @InjectMocks
  private PataviTaskService pataviTaskService;


  @Before
  public void setUp() throws ResourceDoesNotExistException {
    pataviTaskService = new PataviTaskServiceImpl();
    initMocks(this);
    reset(modelService, pataviTaskRepository, problemService);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(modelService, pataviTaskRepository);
  }

  @Test
  public void testFindTaskWhenThereIsNoTask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnexpectedNumberOfResultsException {
    Integer modelId = -2;
    String problem = "Yo";
    Integer projectId = -6;
    Integer analysisId = -7;
    String modelTitle = "modelTitle";

    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .link(Model.LINK_LOG)
            .build();
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = mock(NetworkMetaAnalysisProblem.class);
    when(networkMetaAnalysisProblem.toString()).thenReturn(problem);
    when(problemService.getProblem(projectId, analysisId)).thenReturn(networkMetaAnalysisProblem);
    when(modelService.find(modelId)).thenReturn(model);
    URI createdURI = URI.create("new.task.com");
    when(pataviTaskRepository.createPataviTask(networkMetaAnalysisProblem.buildProblemWithModelSettings(model))).thenReturn(createdURI);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);

    assertNotNull(result.getUri());
    verify(modelService).find(modelId);
    verify(problemService).getProblem(projectId, analysisId);
    verify(pataviTaskRepository).createPataviTask(networkMetaAnalysisProblem.buildProblemWithModelSettings(model));
  }

  @Test
  public void testFindTaskWhenThereAlreadyIsATask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnexpectedNumberOfResultsException {
    Integer modelId = -2;
    Integer projectId = -6;
    Integer analysisId = -7;
    String modelTitle = "modelTitle";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .id(modelId)
            .taskUri(URI.create("-7"))
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    when(modelService.find(modelId)).thenReturn(model);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
    assertNotNull(result.getUri());
    verify(modelService).find(modelId);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testFindTaskForInvalidModel() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnexpectedNumberOfResultsException {
    Integer projectId = -6;
    Integer analysisId = -7;
    Integer invalidModelId = -2;
    when(modelService.find(invalidModelId)).thenReturn(null);
    try{
      pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, invalidModelId);
    }finally {
      verify(modelService).find(invalidModelId);
    }
  }
}
