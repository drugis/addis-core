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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by daan on 2/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
@Transactional
public class ProjectsRepositoryTest {
  @Autowired
  private ProjectRepository projectRepository;

  @PersistenceContext
  EntityManager em;

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
    Account account = new Account(1, "foo@bar.com", "Connor", "Bonnor");
    assertEquals(3, projectRepository.query().size());
    Project project = projectRepository.create(account, "newProjectName", "newProjectDesc", 1);
    assertEquals(project.getOwner(), account);
    Collection<Project> projectList = projectRepository.query();
    assertEquals(4, projectList.size());
    assertNotNull(project.getId());
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetNonexistentProjectFails() throws Exception {
    projectRepository.getProjectById(3213);
  }

  @Test
  public void testGetProjectById() throws Exception {
    Account account = em.find(Account.class, 1);
    Outcome outcome1 = em.find(Outcome.class, 1);
    Outcome outcome2 = em.find(Outcome.class, 2);

    Project project = new Project(1, account, "testname 1", "testdescription 1", 1, new ArrayList<Outcome>());
    project.addOutcome(outcome1);
    project.addOutcome(outcome2);

    Project result = projectRepository.getProjectById(1);
    assertEquals(project, result);
  }

  @Test
  public void testUpdateOutcomes() throws Exception {
    Project project = projectRepository.getProjectById(1);

    Outcome outcomeNew = new Outcome("nameNew", "motivationNew", "URINew");
    project.addOutcome(outcomeNew);

    em.persist(project);

    Project projectUpdated = projectRepository.getProjectById(1);
    assertTrue(projectUpdated.getOutcomes().contains(outcomeNew));
  }

  @Test
  public void testUpdateProject() throws Exception {
    Project project = em.find(Project.class, 1);
    em.detach(project);

    Outcome outcomeNew = new Outcome("nameNew", "motivationNew", "URINew");
    project.addOutcome(outcomeNew);

    Project updated = projectRepository.update(project);

    assertTrue(updated.getOutcomes().contains(outcomeNew));
    assertTrue(em.contains(updated));
  }

}
