package org.drugis.addis.scenarios.repository;

import org.drugis.addis.analyses.State;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.scenarios.Scenario;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

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

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testGet() throws Exception {
    int id = 1;
    Scenario expected = em.find(Scenario.class, id);
    Scenario actual = scenarioRepository.get(id);
    assertEquals(expected, actual);
  }

  @Test
  public void testCreate() {
    int workspaceId = 2;
    Integer scenarioId = 3;
    String title = "title";
    String problem = "Problem";
    Scenario created = scenarioRepository.create(workspaceId, title, new State(problem));
    Scenario found = em.find(Scenario.class, scenarioId);
    assertEquals(found, created);
  }
}
