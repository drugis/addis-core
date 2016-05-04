package org.drugis.trialverse.dataset.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 11-3-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class VersionMappingRepositoryJpaTest {
  @Inject
  VersionMappingRepository versionMappingRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testFindAll() throws Exception {
    List<String> datasetUrls = Arrays.asList("http://trials.drugis.org/datasets/e2ab9670-d3c7-402c-81ad-60abbb46ca4c", "http://trials.drugis.org/datasets/dbdf84e9-8bdb-4233-9bf6-c553cd023638");
    List<VersionMapping> result = versionMappingRepository.findMappingsByTrialverseDatasetUrls(datasetUrls);
    assertEquals(2, result.size());
  }

}
