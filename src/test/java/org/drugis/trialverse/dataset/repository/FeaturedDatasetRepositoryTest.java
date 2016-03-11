package org.drugis.trialverse.dataset.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.trialverse.dataset.model.FeaturedDataset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 11-3-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class FeaturedDatasetRepositoryTest {
  @Inject
  FeaturedDatasetRepository featuredDatasetRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testFindAll() throws Exception {
    FeaturedDataset fd1 =em.find(FeaturedDataset.class, "http://trials.drugis.org/datasets/e2ab9670-d3c7-402c-81ad-60abbb46ca4c");
    FeaturedDataset fd2 =em.find(FeaturedDataset.class, "http://trials.drugis.org/datasets/dbdf84e9-8bdb-4233-9bf6-c553cd023638");

    List<FeaturedDataset> result = featuredDatasetRepository.findAll();
    assertTrue(result.contains(fd1));
    assertTrue(result.contains(fd2));
  }
}