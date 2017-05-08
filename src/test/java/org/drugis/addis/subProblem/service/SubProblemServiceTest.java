package org.drugis.addis.subProblem.service;

import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.subProblem.SubProblem;
import org.drugis.addis.subProblem.repository.SubProblemRepository;
import org.drugis.addis.subProblem.service.impl.SubProblemServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
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

}