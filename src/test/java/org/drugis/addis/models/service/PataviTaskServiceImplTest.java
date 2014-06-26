package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.PataviTask;
import org.drugis.addis.models.PataviTaskUriHolder;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.repository.PataviTaskRepository;
import org.drugis.addis.outcomes.repository.impl.PataviTaskServiceImpl;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PataviTaskServiceImplTest {

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
    modelRepository = mock(ModelRepository.class);
    pataviTaskRepository = mock(PataviTaskRepository.class);
    problemService = mock(ProblemService.class);
    pataviTaskService = new PataviTaskServiceImpl();
    initMocks(this);
    reset(modelRepository, pataviTaskRepository, problemService);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(modelRepository, pataviTaskRepository);
  }

  @Test
  public void testFindTaskWhenThereIsNoTask() throws ResourceDoesNotExistException {
    Integer modelId = -2;
    String uri = "uri";
    Model model = mock(Model.class);
    String problem = "Yo";
    Integer projectId = -6;
    Integer analysisId = -7;
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = mock(NetworkMetaAnalysisProblem.class);
    PataviTask pataviTask = new PataviTask(modelId, problem);
    when(networkMetaAnalysisProblem.toString()).thenReturn(problem);
    when(problemService.getProblem(projectId, analysisId)).thenReturn(networkMetaAnalysisProblem);
    when(modelRepository.find(modelId)).thenReturn(model);
    when(pataviTaskRepository.findPataviTask(modelId)).thenReturn(null);
    when(pataviTaskRepository.createPataviTask(modelId, networkMetaAnalysisProblem)).thenReturn(pataviTask);
    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
    assertTrue(!result.getUri().isEmpty());
    verify(modelRepository).find(modelId);
    verify(pataviTaskRepository).findPataviTask(modelId);
    verify(problemService).getProblem(projectId,analysisId);
    verify(pataviTaskRepository).createPataviTask(modelId, networkMetaAnalysisProblem);
  }

  @Test
  public void testFindTaskWhenThereAlreadyIsATask() throws ResourceDoesNotExistException {
    Integer modelId = -2;
    String uri = "uri";
    String problem = "Yo";
    Model model = mock(Model.class);
    Integer projectId = -6;
    Integer analysisId = -7;
    PataviTask pataviTask = new PataviTask(modelId, problem);
    when(modelRepository.find(modelId)).thenReturn(model);
    when(pataviTaskRepository.findPataviTask(modelId)).thenReturn(pataviTask);
    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
    assertTrue(!result.getUri().isEmpty());
    verify(modelRepository).find(modelId);
    verify(pataviTaskRepository).findPataviTask(modelId);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testFindTaskForInvalidModel() throws ResourceDoesNotExistException {
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