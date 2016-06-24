package org.drugis.addis.projects;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by daan on 2/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ProjectsRepositoryTest {
  @Autowired
  private ProjectRepository projectRepository;

  @Test
  public void testQuery() {
    Collection<Project> projects = projectRepository.query();
    assertEquals(3, projects.size());
  }

  @Test
  public void testQueryByOwner() {
    Collection<Project> projects = projectRepository.queryByOwnerId(1);
    assertEquals(2, projects.size());
  }

  @Test
  public void testCreate() {
    Account account = new Account(1, "1000012345", "Connor", "Bonnor", "connor@test.com");
    assertEquals(3, projectRepository.query().size());
    Project project = projectRepository.create(account, new ProjectCommand("newProjectName", "newProjectDesc", "uid1", "version 1"));
    assertEquals(project.getOwner(), account);
    Collection<Project> projectList = projectRepository.query();
    assertEquals(4, projectList.size());
    assertNotNull(project.getId());
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetNonexistentProjectFails() throws Exception {
    projectRepository.get(3213);
  }

  @Test
  public void testGetProjectById() throws Exception {
    Project result = projectRepository.get(1);

    assertEquals(new Integer(1), result.getId());
    assertEquals("testname 1", result.getName());
    assertEquals("testdescription 1", result.getDescription());
  }

  @Test
  public void isExistingProjectName() {
    assertTrue(projectRepository.isExistingProjectName(3, "testname 1")); // duplicate name for owner
    assertFalse(projectRepository.isExistingProjectName(3, "updated")); // non duplicate name for owner
    assertFalse(projectRepository.isExistingProjectName(2, "testname 1")); // duplicate name for non owner
  }

}
