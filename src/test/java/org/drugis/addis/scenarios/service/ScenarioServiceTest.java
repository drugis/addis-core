package org.drugis.addis.scenarios.service;

import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.service.impl.ScenarioServiceImpl;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 16-4-14.
 */
public class ScenarioServiceTest {
  @Mock
  ProjectRepository projectRepository;

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  SubProblemRepository subProblemRepository;

  @InjectMocks
  private ScenarioService scenarioService;

  private Integer projectId = 1;
  private Integer analysisId = 2;
  private Project project = mock(Project.class);
  private BenefitRiskAnalysis analysis = mock(BenefitRiskAnalysis.class);
  private Integer subProblemId = 100;
  private Scenario scenario = new Scenario(2, subProblemId, "title", "state");
  private SubProblem subProblem = new SubProblem(subProblemId, analysisId, "definition", "title");

  @Before
  public void setUp() throws ResourceDoesNotExistException {
    projectRepository = mock(ProjectRepository.class);
    analysisRepository = mock(AnalysisRepository.class);
    subProblemRepository = mock(SubProblemRepository.class);

    scenarioService = new ScenarioServiceImpl();

    initMocks(this);

    when(analysis.getProjectId()).thenReturn(projectId);

    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(subProblemRepository.get(subProblemId)).thenReturn(subProblem);
  }

  @Test
  public void testCheckCoordinates() throws Exception {
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId, scenario);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCheckAnalysisNotInProject() throws ResourceDoesNotExistException {
    when(analysis.getProjectId()).thenReturn(projectId + 1);
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId, scenario);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testSubProblemNotInAnalysis() throws ResourceDoesNotExistException {
    when(subProblemRepository.get(subProblemId + 1)).thenReturn(new SubProblem(
            subProblemId + 1, analysisId + 1, "definition", "title"));
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId + 1, scenario);
  }
  @Test(expected = ResourceDoesNotExistException.class)
  public void testScenarioNotInSubProblem() throws ResourceDoesNotExistException {
    Scenario newScenario = new Scenario(2, subProblemId + 1, "title", "state");
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId, newScenario);
  }

}
