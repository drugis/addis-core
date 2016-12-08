package org.drugis.addis.interventions;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

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
    Collection<AbstractIntervention> interventions = interventionRepository.query(1);
    assertEquals(2, interventions.size());
    interventions = interventionRepository.query(2);
    assertEquals(4, interventions.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    int interventionId = -1;
    AbstractIntervention intervention = interventionRepository.get(interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof SimpleIntervention);

    interventionId = -4;
    intervention = interventionRepository.get(interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof FixedDoseIntervention);

    interventionId = -5;
    intervention = interventionRepository.get(interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof TitratedDoseIntervention);

    interventionId = -6;
    intervention = interventionRepository.get(interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof CombinationIntervention);
  }

  @Test
  public void testGetByProject() throws ResourceDoesNotExistException {
    int interventionId = -1;
    AbstractIntervention intervention = interventionRepository.get(1, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof SimpleIntervention);

    interventionId = -4;
    intervention = interventionRepository.get(2, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof FixedDoseIntervention);

    interventionId = -5;
    intervention = interventionRepository.get(2, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof TitratedDoseIntervention);

    interventionId = -6;
    intervention = interventionRepository.get(2, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert (intervention instanceof CombinationIntervention);
  }

  @Test
  public void fixedDoseIntervention() throws InvalidConstraintException {
    DoseConstraint constraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, 1d, "unit", "P1d", URI.create("uriConcept")),
            new UpperBoundCommand(UpperBoundType.AT_MOST, 2d, "unit", "P1d", URI.create("uriConcept")));

    FixedDoseIntervention t = new FixedDoseIntervention(null, 1, "tit", "moti", URI.create("semuri"), "semlabel",
            constraint);
    em.persist(t);

    FixedDoseIntervention result = em.find(FixedDoseIntervention.class, t.getId());
    assertEquals(t, result);
  }

  @Test
  public void titratedDoseIntervention() throws InvalidConstraintException {
    DoseConstraint minConstraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, 1d, "unit", "P1D", URI.create("uriConcept")),
            new UpperBoundCommand(UpperBoundType.AT_MOST, 2d, "unit", "P1D", URI.create("uriConcept")));
    DoseConstraint maxContraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, 1d, "unit", "P1D", URI.create("uriConcept")),
            new UpperBoundCommand(UpperBoundType.AT_MOST, 2d, "unit", "P1D", URI.create("uriConcept")));
    TitratedDoseIntervention t = new TitratedDoseIntervention(null, 1, "tit", "moti", URI.create("semuri"), "semlabel",
            minConstraint, maxContraint);
    em.persist(t);

    TitratedDoseIntervention result = em.find(TitratedDoseIntervention.class, t.getId());
    assertEquals(t, result);
  }

  @Test
  public void bothDoseIntervention() throws InvalidConstraintException {
    DoseConstraint minConstraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, 1d, "unit", "P1D", URI.create("uriConcept")),
            new UpperBoundCommand(UpperBoundType.AT_MOST, 2d, "unit", "P1D", URI.create("uriConcept")));
    DoseConstraint maxContraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, 1d, "unit", "P1D", URI.create("uriConcept")),
            new UpperBoundCommand(UpperBoundType.AT_MOST, 2d, "unit", "P1D", URI.create("uriConcept")));
    BothDoseTypesIntervention t = new BothDoseTypesIntervention(null, 1, "tit", "moti", URI.create("semuri"), "semlabel",
            minConstraint, maxContraint);
    em.persist(t);

    BothDoseTypesIntervention result = em.find(BothDoseTypesIntervention.class, t.getId());
    assertEquals(t, result);
  }

  @Test
  public void createCombinedInterventionTest() throws ResourceDoesNotExistException {
    Integer projectId = 1;

    Set<Integer> combination = new HashSet<>();
    combination.add(-1);
    combination.add(-5);
    CombinationIntervention combinationIntervention = new CombinationIntervention(null, projectId, "intervention name", "motivation", combination);
    em.persist(combinationIntervention);


    CombinationIntervention result = em.find(CombinationIntervention.class, combinationIntervention.getId());
    assertEquals(combinationIntervention, result);
    assertEquals(2, result.getInterventionIds().size());
  }


  @Test
  public void testCreateSimpleIntervention() throws Exception, InvalidConstraintException {
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "newName", "newMotivation", "http://semantic.com", "labelnew");
    Account user = em.find(Account.class, 1);
    AbstractIntervention result = interventionRepository.create(user, interventionCommand);
    assertTrue(interventionRepository.query(1).contains(result));
  }

  @Test
  public void testCreateTitratedIntervention() throws Exception, InvalidConstraintException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 3.0, "mg", "P1D", URI.create("uriConcept"));
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 4.5, "mg", "P1D", URI.create("uriConcept"));
    ConstraintCommand minConstraintCommand = new ConstraintCommand(lowerBound, upperBound);
    AbstractInterventionCommand interventionCommand = new TitratedInterventionCommand(1, "newName", "newMotivation",
            "http://semantic.com", "labelnew", minConstraintCommand, null);
    Account user = em.find(Account.class, 1);
    AbstractIntervention result = interventionRepository.create(user, interventionCommand);
    assertTrue(interventionRepository.query(1).contains(result));
  }


  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateInterventionInNotOwnedProject() throws Exception, InvalidConstraintException {
    Account account = em.find(Account.class, 2);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "newName", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(account, interventionCommand);
  }


  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateInterventionInNonexistentProject() throws Exception, InvalidConstraintException {
    Account account = em.find(Account.class, 2);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(13221, "newName", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(account, interventionCommand);
  }

  @Test(expected = InvalidDataAccessApiUsageException.class)
  public void testCreateWithDuplicateNameFails() throws Exception, InvalidConstraintException {
    Account user = em.find(Account.class, 1);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "intervention 1", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(user, interventionCommand);
  }

  @Test
  public void isExistingNameWhenUseTheSameName() {
    boolean result = interventionRepository.isExistingInterventionName(-2, "intervention 2");
    assertFalse(result);
  }

  @Test
  public void isExistingNameWhenUseTheAExistingName() {
    boolean result = interventionRepository.isExistingInterventionName(-2, "intervention 1");
    assertTrue(result);
  }

  @Test
  public void isExistingNameWhenUseTheANewName() {
    boolean result = interventionRepository.isExistingInterventionName(-2, "new name");
    assertFalse(result);
  }

  @Test
  public void testDelete() throws ResourceDoesNotExistException {
    interventionRepository.delete(-1);
    AbstractIntervention abstractIntervention = interventionRepository.get(-1);
    assertNull(abstractIntervention);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testDeleteNonexistent() throws ResourceDoesNotExistException {
    interventionRepository.delete(-37);
  }
}
