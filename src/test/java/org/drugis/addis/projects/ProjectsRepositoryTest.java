package org.drugis.addis.projects;

import org.drugis.addis.config.RepositoryTestConfig;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.mockito.Mockito.*;
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

  @Ignore
  @Test
  public void testCreate() {
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(1);
    Project project = projectRepository.create(account, "newProjectName", "newProjectDesc", "newTrialVerseNamespace");
    assertEquals(project.getOwner(), account);
  }
}
