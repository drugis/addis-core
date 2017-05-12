package org.drugis.addis.subProblems.service;

import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.drugis.addis.subProblems.service.impl.SubProblemServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by joris on 8-5-17.
 */
public class SubProblemServiceTest {

  @Mock
  ScenarioRepository scenarioRepository;

  @Mock
  private SubProblemRepository subProblemRepository;

  @InjectMocks
  private SubProblemService subProblemService = new SubProblemServiceImpl();

  @Before
  public void setUp() {
    initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(subProblemRepository, scenarioRepository);
  }

  @Test
  public void testCreateMCDADefaults() {
    Integer projectId = 1;
    Integer analysisId = 10;
    String scenarioState = "its: a scenario";
    Integer subProblemId = 100;
    SubProblem subProblem = new SubProblem(subProblemId, analysisId, "{}", "Default");
    when(subProblemRepository.create(analysisId, "{}", "Default")).thenReturn(subProblem);

    subProblemService.createMCDADefaults(projectId, analysisId, scenarioState);

    verify(subProblemRepository).create(analysisId, "{}", "Default");
    verify(scenarioRepository).create(analysisId, subProblemId, Scenario.DEFAULT_TITLE, scenarioState);
  }

  @Test
  public void testCreateSubProblem() {
    Integer analysisId = 1;
    String problemDefinition = "{}";
    String title = "title";
    String scenarioState = "{\"P\": \"p\"}";

    Integer newSubProblemId = 3;
    SubProblem newSubProblem = new SubProblem(newSubProblemId, analysisId, problemDefinition, title);
    when(subProblemRepository.create(analysisId, problemDefinition, title)).thenReturn(newSubProblem);

    SubProblem subProblem = subProblemService.createSubProblem(analysisId, problemDefinition, title, scenarioState);

    verify(subProblemRepository).create(analysisId, problemDefinition, title);
    verify(scenarioRepository).create(analysisId, newSubProblemId, Scenario.DEFAULT_TITLE, scenarioState);
  }

}