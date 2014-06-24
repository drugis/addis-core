package org.drugis.addis.scenarios.service;

import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.scenarios.service.impl.ScenarioServiceImpl;
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
  ScenarioRepository scenarioRepository;

  @InjectMocks
  ScenarioService scenarioService;

  private Integer projectId = 1;
  private Integer analysisId = 2;
  private Scenario scenario = new Scenario(2, "title", "state");
  private Project project = mock(Project.class);
  private SingleStudyBenefitRiskAnalysis analysis = mock(SingleStudyBenefitRiskAnalysis.class);

  @Before
  public void setUp() throws ResourceDoesNotExistException {
    projectRepository = mock(ProjectRepository.class);
    analysisRepository = mock(AnalysisRepository.class);
    scenarioRepository = mock(ScenarioRepository.class);

    scenarioService = new ScenarioServiceImpl();

    initMocks(this);

    when(analysis.getProjectId()).thenReturn(projectId);

    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(analysis);
  }

  @Test
  public void testCheckCoordinates() throws Exception {
    scenarioService.checkCoordinates(projectId, analysisId, scenario);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCheckAnalysisNotInProject() throws ResourceDoesNotExistException {
    when(analysis.getProjectId()).thenReturn(projectId + 1);

    scenarioService.checkCoordinates(projectId, analysisId, scenario);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCheckScenarioNotInAnalysis() throws ResourceDoesNotExistException {
    Scenario wrongAnalysisScenario = new Scenario(3, "title", "state");
    scenarioService.checkCoordinates(projectId, analysisId, wrongAnalysisScenario);
  }

}
