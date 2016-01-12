package org.drugis.addis.patavitask;

/**
 * Created by connor on 9/25/15.
 */

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class PataviTaskRepositoryImplIntergrationTest {


  @Inject
  private PataviTaskRepository pataviTaskRepository;

  @Test
  public void testGet(){
    PataviTask pataviTask = pataviTaskRepository.get(1);
    assertNotNull(pataviTask);
  }

  @Test(expected = EmptyResultDataAccessException.class)
  public void testGetNonExistent(){
    PataviTask emptyResult = pataviTaskRepository.get(-999);
    assertNotNull(emptyResult);
  }

  @Test
  public void testFindByIds() throws SQLException {
    List<Integer> ids = Arrays.asList(1, 2);
    List<PataviTask> tasks = pataviTaskRepository.findByIds(ids);
    assertTrue(tasks.get(0).isHasResult());
    assertFalse(tasks.get(1).isHasResult());
  }

  @Test(expected = EmptyResultDataAccessException.class)
  public void testDelete() throws SQLException {
    pataviTaskRepository.delete(1);
    pataviTaskRepository.get(1);
  }

  @Test
  public void testGetResult() throws IOException, UnexpectedNumberOfResultsException {
    JsonNode result = pataviTaskRepository.getResult(1);
    assertNotNull(result);
    assertEquals("some results", result.asText());
  }

  @Test(expected = UnexpectedNumberOfResultsException.class)
  public void testGetResultWhenThereAreNone() throws IOException, UnexpectedNumberOfResultsException {
    pataviTaskRepository.getResult(2);
  }
}
