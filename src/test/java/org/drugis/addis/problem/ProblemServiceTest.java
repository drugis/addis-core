package org.drugis.addis.problem;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problem.service.ProblemService;
import org.drugis.addis.problem.service.impl.ProblemServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @InjectMocks
  ProblemService problemService;

  @Before
  public void setUp() {
    problemService = new ProblemServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetProblem() throws ResourceDoesNotExistException {
    int projectId = 1;
    int analysisId = 1;
    Analysis analysis = mock(Analysis.class);
    when(analysis.getName()).thenReturn("testName");
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
    Problem actualProblem = problemService.getProblem(projectId, analysisId);
    Problem expectedProblem = new Problem(analysis.getName());
    assertEquals(expectedProblem, actualProblem);
    verify(analysisRepository).get(projectId, analysisId);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(analysisRepository);
  }
}
