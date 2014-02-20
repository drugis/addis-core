package org.drugis.addis.projects;

import org.drugis.addis.config.RepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.Trialverse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by daan on 2/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RepositoryTestConfig.class})
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
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(1);
    assertEquals(3, projectRepository.query().size());
    Project project = projectRepository.create(account, "newProjectName", "newProjectDesc", new Trialverse("newTrialVerseNamespace"));
    assertEquals(project.getOwner(), account);
    assertEquals(4, projectRepository.query().size());
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetNonexistentProjectFails() throws Exception {
    projectRepository.getProjectById(3213);
  }

  @Test
  public void testGetProjectById() throws Exception {
    Account account = new Account(1, "foo@bar.com", "Connor", "Bonnor");
    Project project = new Project(1, account, "testname 1", "testdescription 1", new Trialverse("org.drugis.addis.trialverse://testtrialverse1"));
    Project result = projectRepository.getProjectById(1);
    assertEquals(project, result);
  }
}
