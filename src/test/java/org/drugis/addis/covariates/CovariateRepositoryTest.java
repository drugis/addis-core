package org.drugis.addis.covariates;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 12/2/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class CovariateRepositoryTest {

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  private CovariateRepository covariateRepository;

  @Test
  public void findByProjectTest() {
    Collection<Covariate> covariates = covariateRepository.findByProject(1);
    assertEquals(covariates.size(), 2);
  }

  @Test
  public void createForProjectTest() {
    covariateRepository.createForProject(3, CovariateOption.ALLOCATION_RANDOMIZED, "add name", null);
    Collection<Covariate> covariates = covariateRepository.findByProject(3);
    assertEquals(covariates.size(), 1);
    assertEquals(covariates.iterator().next().getName(), "add name");
  }
}
