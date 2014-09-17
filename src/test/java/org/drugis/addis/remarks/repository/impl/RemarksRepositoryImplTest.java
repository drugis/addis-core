package org.drugis.addis.remarks.repository.impl;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class RemarksRepositoryImplTest {

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  RemarksRepository remarksRepository;

  @Test
  public void testGet() throws Exception {
    Integer scenarioId = 1;
    Integer remarksId = -1;
    Remarks expected = em.find(Remarks.class, remarksId);

    Remarks actual = remarksRepository.get(scenarioId);
    assertEquals(expected, actual);
  }
}