package org.drugis.addis.outcomes;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by daan on 3/7/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class OutcomeRepositoryTest {
  @Inject
  private OutcomeRepository outcomeRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testQuery() {
    Collection<Outcome> outcomes = outcomeRepository.query(1);
    assertEquals(2, outcomes.size());
    outcomes = outcomeRepository.query(2);
    assertEquals(1, outcomes.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    Outcome outcome = outcomeRepository.get(1, 1);
    assertEquals(em.find(Outcome.class, 1), outcome);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    outcomeRepository.get(2, 1);
  }

  @Test
  public void testGetByProjectAndIds() throws ResourceDoesNotExistException {
    List<Outcome> outcome = outcomeRepository.get(1, Arrays.asList(1, 2));
    assertEquals(2, outcome.size());
  }

  @Test
  public void testCreateOutcome() throws Exception {
    Account user = em.find(Account.class, 1);
    Outcome result = outcomeRepository.create(user, 1, "newName 1", 1,"newMotivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
    assertTrue(outcomeRepository.query(1).contains(result));
  }


  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateOutcomeInNotOwnedProject() throws Exception {
    Account account = em.find(Account.class, 2);
    outcomeRepository.create(account, 1, "newName 2", 1, "newMotivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
  }


  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateOutcomeInNonexistentProject() throws Exception {
    Account account = em.find(Account.class, 2);
    outcomeRepository.create(account, 13221, "newName 3", 1, "newMotivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
  }

  @Test(expected = Exception.class)
  public void testCannotCreateOutcomeWithDuplicateName() throws Exception {
    Account account = em.find(Account.class, 1);
    outcomeRepository.create(account, 1, "outcome 1", 1,"newMotivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
  }

}
