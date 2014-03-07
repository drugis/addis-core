package org.drugis.addis.projects;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.InterventionCommand;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

  @PersistenceContext(unitName = "addisCore")
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
    Project result = projectRepository.getProjectById(1);

    assertEquals(new Integer(1), result.getId());
    assertEquals("testname 1", result.getName());
    assertEquals("testdescription 1", result.getDescription());
    em.refresh(result);
    assertEquals(2, result.getOutcomes().size());
    assertEquals(2, result.getInterventions().size());
  }

  @Test
  public void getProjectOutcome() throws Exception {
    Outcome expected = em.find(Outcome.class, 1);
    Outcome result = projectRepository.getProjectOutcome(1, 1);
    assertEquals(expected, result);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testOutcomeFromWrongProjectFails() throws Exception {
    projectRepository.getProjectOutcome(2, 1);
  }

  @Test
  public void testCreateOutcome() throws Exception {
    OutcomeCommand outcomeCommand = new OutcomeCommand("newName", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    Account user = em.find(Account.class, 1);
    Outcome result = projectRepository.createOutcome(user, 1, outcomeCommand);
    assertNotNull(result);
    assertEquals(outcomeCommand.getName(), result.getName());
    assertEquals(outcomeCommand.getMotivation(), result.getMotivation());
    assertEquals(outcomeCommand.getSemanticOutcome().getLabel(), result.getSemanticOutcomeLabel());
    assertEquals(outcomeCommand.getSemanticOutcome().getUri(), result.getSemanticOutcomeUrl());
    em.persist(result);
    em.flush();
    Project project = em.find(Project.class, 1);
    assertTrue(project.getOutcomes().contains(result));
  }

  @Test
  public void getProjectIntervention() throws Exception {
    Intervention expected = em.find(Intervention.class, 1);
    Intervention result = projectRepository.getProjectIntervention(1, 1);
    assertEquals(expected, result);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testInterventionFromWrongProjectFails() throws Exception {
    projectRepository.getProjectIntervention(2, 1);
  }

  @Test
  public void testCreateIntervention() throws Exception {
    InterventionCommand interventionCommand = new InterventionCommand("newName", "newMotivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Account user = em.find(Account.class, 1);
    Intervention result = projectRepository.createIntervention(user, 1, interventionCommand);
    assertNotNull(result);
    assertEquals(interventionCommand.getName(), result.getName());
    assertEquals(interventionCommand.getMotivation(), result.getMotivation());
    assertEquals(interventionCommand.getSemanticIntervention().getLabel(), result.getSemanticInterventionLabel());
    assertEquals(interventionCommand.getSemanticIntervention().getUri(), result.getSemanticInterventionUrl());
    em.persist(result);
    em.flush();
    Project project = em.find(Project.class, 1);
    assertTrue(project.getInterventions().contains(result));
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateOutcomeInNotOwnedProject() throws Exception {
    Account account = em.find(Account.class, 2);
    OutcomeCommand outcomeCommand = new OutcomeCommand("newName", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    projectRepository.createOutcome(account, 1, outcomeCommand);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateOutcomeInNonexistentProject() throws Exception {
    Account account = em.find(Account.class, 2);
    OutcomeCommand outcomeCommand = new OutcomeCommand("newName", "newMotivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    projectRepository.createOutcome(account, 134957862, outcomeCommand);
  }
}
