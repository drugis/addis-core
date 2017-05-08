package org.drugis.addis.scenarios.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.scenarios.Scenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by connor on 3-4-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ScenarioRepositoryTest {
  @Inject
  ScenarioRepository scenarioRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;
  private final Integer projectId = 1;
  private Integer subProblemId = 100;

  @Test
  public void testGet() throws Exception {
    int id = 1;
    Scenario expected = em.find(Scenario.class, id);
    Scenario actual = scenarioRepository.get(id);
    assertEquals(expected, actual);
  }

  @Test
  public void testCreate() {
    int workspaceId = -2;
    String title = "title";
    String problem = "Problem";
    Scenario created = scenarioRepository.create(workspaceId, subProblemId, title, problem);
    Scenario found = em.find(Scenario.class, created.getId());
    assertEquals(found, created);
  }

  @Test
  public void testQueryByProjectAndAnalysis() {
    Integer analysisId = -1;
    Collection<Scenario> result = scenarioRepository.queryBySubProblem(projectId, analysisId, subProblemId);
    assertNotNull(result);
    assertEquals(2, result.size());
    Scenario defaultScenario = em.find(Scenario.class, 1);
    assertTrue(result.contains(defaultScenario));
  }

  @Test
  public void testQueryByProject() {
    Collection<Scenario> result = scenarioRepository.queryByProject(projectId);
    assertNotNull(result);
    assertEquals(4, result.size());
  }

  @Test
  public void testQueryFromMetaBenefitRisk() {
    Integer analysisId = -10;
    Collection<Scenario> result = scenarioRepository.queryBySubProblem(projectId, analysisId, subProblemId);
    assertNotNull(result);
    assertEquals(1, result.size());
    Scenario defaultScenario = em.find(Scenario.class, 4);
    assertTrue(result.contains(defaultScenario));
  }

  @Test
  public void testUpdate() {
    String newTitle = "new title";
    Integer scenarioId = 1;
    String newState = "{\"newKey\":\"newValue\"}";
    Scenario result = scenarioRepository.update(scenarioId, newTitle, newState);
    Scenario updated = em.find(Scenario.class, scenarioId);
    assertEquals(newTitle, updated.getTitle());
    assertEquals(newState, updated.getState());
    assertEquals(result, updated);
  }

}
