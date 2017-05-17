package org.drugis.addis.subProblems.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.subProblems.SubProblem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by joris on 8-5-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class SubProblemRepositoryTest {

  private final SubProblem defaultSubProblem = new SubProblem(100, -1, "{}", "Default");
  @Inject
  private SubProblemRepository subProblemRepository;
  private Integer workspaceId = -1;
  private Integer projectId = 1;

  @Test
  public void testCreate() {
    String definition = "{}";
    String title = "not default";
    SubProblem result = subProblemRepository.create(workspaceId, definition, title);
    assertEquals(workspaceId, result.getWorkspaceId());
    assertEquals(definition, result.getDefinition());
    assertEquals(title, result.getTitle());
  }

  @Test
  public void testQueryByProject() {
    Collection<SubProblem> subProblems = subProblemRepository.queryByProject(projectId);

    SubProblem expectedProblem1 = defaultSubProblem;
    SubProblem expectedProblem2 = new SubProblem(101, -2, "{}", "Default");
    SubProblem expectedProblem3 = new SubProblem(102, -10, "{}", "Default");
    assertEquals(3, subProblems.size());
    assertEquals(Arrays.asList(expectedProblem1, expectedProblem2,  expectedProblem3), subProblems);
  }

  @Test
  public void testQueryByProjectAndAnalysis() {
    Collection<SubProblem> subProblems = subProblemRepository.queryByProjectAndAnalysis(projectId, workspaceId);

    SubProblem expectedProblem = defaultSubProblem;
    assertEquals(1, subProblems.size());
    assertEquals(expectedProblem, subProblems.iterator().next());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    SubProblem subProblem = subProblemRepository.get(100);
    assertEquals(defaultSubProblem, subProblem);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testMissingException() throws ResourceDoesNotExistException {
    subProblemRepository.get(-11100);
  }

}