package org.drugis.addis.trialverse;

import org.drugis.addis.config.JpaTrialverseRepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 2/26/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaTrialverseRepositoryTestConfig.class})
@Transactional
public class TrialverseRepositoryTest {
  @Inject
  private TrialverseRepository trialverseRepository;

  @Test
  public void testQuery() {
    Collection<Trialverse> trialverses = trialverseRepository.query();
    assertEquals(3, trialverses.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    Trialverse trialverse = trialverseRepository.get(1L);
    assertEquals(new Long(1), trialverse.getId());
  }

}
