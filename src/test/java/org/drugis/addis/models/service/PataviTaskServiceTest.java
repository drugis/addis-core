package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
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
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

;

public class PataviTaskServiceTest {
  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private ProblemService problemService;

  @Mock
  private ModelRepository modelRepository;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @InjectMocks
  private PataviTaskService pataviTaskService;


  @Before
  public void setUp() throws ResourceDoesNotExistException {
    pataviTaskService = new PataviTaskServiceImpl();
    initMocks(this);
    reset(modelRepository, pataviTaskRepository, problemService);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(modelRepository, pataviTaskRepository);
  }

  @Test
  public void testFindTaskWhenThereIsNoTask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException {
    Integer modelId = -2;
    String problem = "Yo";
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
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = mock(NetworkMetaAnalysisProblem.class);
    PataviTask pataviTask = new PataviTask(PataviTaskRepositoryImpl.GEMTC_METHOD, problem);
    when(networkMetaAnalysisProblem.toString()).thenReturn(problem);
    when(problemService.getProblem(projectId, analysisId)).thenReturn(networkMetaAnalysisProblem);
    when(modelRepository.find(modelId)).thenReturn(model);
    when(pataviTaskRepository.createPataviTask(networkMetaAnalysisProblem, model)).thenReturn(pataviTask);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);

    assertTrue(!result.getUri().isEmpty());
    verify(modelRepository).find(modelId);
    verify(problemService).getProblem(projectId, analysisId);
    verify(pataviTaskRepository).createPataviTask(networkMetaAnalysisProblem, model);
  }

  @Test
  public void testFindTaskWhenThereAlreadyIsATask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException {
    Integer modelId = -2;
    Integer projectId = -6;
    Integer analysisId = -7;
    String modelTitle = "modelTitle";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    String modelType = Model.NETWORK_MODEL_TYPE;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .id(modelId)
            .taskId(-7)
            .linearModel(linearModel)
            .modelType(modelType)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    when(modelRepository.find(modelId)).thenReturn(model);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
    assertTrue(!result.getUri().isEmpty());
    verify(modelRepository).find(modelId);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testFindTaskForInvalidModel() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException {
    Integer projectId = -6;
    Integer analysisId = -7;
    Integer invalidModelId = -2;
    when(modelRepository.find(invalidModelId)).thenReturn(null);
    try{
      pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, invalidModelId);
    }finally {
      verify(modelRepository).find(invalidModelId);
    }
  }


}
