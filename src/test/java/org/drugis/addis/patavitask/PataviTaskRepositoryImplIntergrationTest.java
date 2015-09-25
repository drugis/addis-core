package org.drugis.addis.patavitask;

/**
 * Created by connor on 9/25/15.
 */

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class PataviTaskRepositoryImplIntergrationTest {


    @Inject
    private PataviTaskRepository pataviTaskRepository;


  @Test()
  public void testFindByIds() throws SQLException {
    List<Integer> ids = Arrays.asList(1, 2);
    List<PataviTask> tasks = pataviTaskRepository.findByIds(ids);
    assertTrue(tasks.get(0).isHasResult());
    assertFalse(tasks.get(1).isHasResult());
  }
}
