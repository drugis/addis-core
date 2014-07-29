package org.drugis.addis.interventions;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by daan on 3/7/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class InterventionRepositoryTest {
  @Inject
  private InterventionRepository interventionRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testQuery() {
    Collection<Intervention> interventions = interventionRepository.query(1);
    assertEquals(2, interventions.size());
    interventions = interventionRepository.query(2);
    assertEquals(1, interventions.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    Intervention intervention = interventionRepository.get(1, 1);
    assertEquals(em.find(Intervention.class, 1), intervention);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    interventionRepository.get(2, 1);
  }

  @Test
  public void testCreateIntervention() throws Exception {
    InterventionCommand interventionCommand = new InterventionCommand(1, "newName", "newMotivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Account user = em.find(Account.class, 1);
    Intervention result = interventionRepository.create(user, interventionCommand);
    assertTrue(interventionRepository.query(1).contains(result));
  }


  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateInterventionInNotOwnedProject() throws Exception {
    Account account = em.find(Account.class, 2);
    InterventionCommand interventionCommand = new InterventionCommand(1, "newName", "newMotivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    interventionRepository.create(account, interventionCommand);
  }


  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateInterventionInNonexistentProject() throws Exception {
    Account account = em.find(Account.class, 2);
    InterventionCommand interventionCommand = new InterventionCommand(13221, "newName", "newMotivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    interventionRepository.create(account, interventionCommand);
  }

  @Test(expected = InvalidDataAccessApiUsageException.class)
  public void testCreateWithDuplicateNameFails() throws Exception {
    Account user = em.find(Account.class, 1);
    InterventionCommand interventionCommand = new InterventionCommand(1, "intervention 1", "newMotivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Intervention result = interventionRepository.create(user, interventionCommand);
  }


}
