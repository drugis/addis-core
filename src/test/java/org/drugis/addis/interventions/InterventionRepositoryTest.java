package org.drugis.addis.interventions;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.security.Account;
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
    Collection<AbstractIntervention> interventions = interventionRepository.query(1);
    assertEquals(2, interventions.size());
    interventions = interventionRepository.query(2);
    assertEquals(3, interventions.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    int interventionId = -1;
    AbstractIntervention intervention = interventionRepository.get(1, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert(intervention instanceof Intervention);

    interventionId = -4;
    intervention = interventionRepository.get(2, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert(intervention instanceof FixedDoseIntervention);

    interventionId = -5;
    intervention = interventionRepository.get(2, interventionId);
    assertEquals(em.find(AbstractIntervention.class, interventionId), intervention);
    assert(intervention instanceof TitratedDoseIntervention);
  }

  @Test
  public void fixedDoseIntervention(){
    DoseConstraint constraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, "unit", 1d),
            new UpperBoundCommand(UpperBoundType.AT_MOST, "unit", 2d));

    FixedDoseIntervention t = new FixedDoseIntervention(null, 1, "tit", "moti", "semuri", "semlabel",
            constraint);
    em.persist(t);

    FixedDoseIntervention result = em.find(FixedDoseIntervention.class,t.getId());
    assertEquals(t, result);
  }

  @Test
  public void titratedDoseIntervention(){
    DoseConstraint minConstraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, "unit", 1d),
            new UpperBoundCommand(UpperBoundType.AT_MOST, "unit", 2d));
    DoseConstraint maxContraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, "unit", 1d),
            new UpperBoundCommand(UpperBoundType.AT_MOST, "unit", 2d));
    TitratedDoseIntervention t = new TitratedDoseIntervention(null, 1, "tit", "moti", "semuri", "semlabel",
            minConstraint, maxContraint);
    em.persist(t);

    TitratedDoseIntervention result = em.find(TitratedDoseIntervention.class,t.getId());
    assertEquals(t, result);
  }

  @Test
  public void bothDoseIntervention(){
    DoseConstraint minConstraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, "unit", 1d),
            new UpperBoundCommand(UpperBoundType.AT_MOST, "unit", 2d));
    DoseConstraint maxContraint = new DoseConstraint(
            new LowerBoundCommand(LowerBoundType.AT_LEAST, "unit", 1d),
            new UpperBoundCommand(UpperBoundType.AT_MOST, "unit", 2d));
    BothDoseTypesIntervention t = new BothDoseTypesIntervention(null, 1, "tit", "moti", "semuri", "semlabel",
            minConstraint, maxContraint);
    em.persist(t);

    BothDoseTypesIntervention result = em.find(BothDoseTypesIntervention.class,t.getId());
    assertEquals(t, result);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    interventionRepository.get(2, -1);
  }

  @Test
  public void testCreateSimpleIntervention() throws Exception {
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "newName", "newMotivation", "http://semantic.com", "labelnew");
    Account user = em.find(Account.class, 1);
    AbstractIntervention result = interventionRepository.create(user, interventionCommand);
    assertTrue(interventionRepository.query(1).contains(result));
  }

  @Test
  public void testCreateTitratedIntervention() throws Exception {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, "mg", 3.0);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, "mg", 4.5);
    ConstraintCommand minConstraintCommand = new ConstraintCommand(lowerBound, upperBound);
    AbstractInterventionCommand interventionCommand = new TitratedInterventionCommand(1, "newName", "newMotivation",
            "http://semantic.com", "labelnew", minConstraintCommand, null);
    Account user = em.find(Account.class, 1);
    AbstractIntervention result = interventionRepository.create(user, interventionCommand);
    assertTrue(interventionRepository.query(1).contains(result));
  }


  @Test(expected = MethodNotAllowedException.class)
  public void testCannotCreateInterventionInNotOwnedProject() throws Exception {
    Account account = em.find(Account.class, 2);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "newName", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(account, interventionCommand);
  }


  @Test(expected = ResourceDoesNotExistException.class)
  public void testCannotCreateInterventionInNonexistentProject() throws Exception {
    Account account = em.find(Account.class, 2);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(13221, "newName", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(account, interventionCommand);
  }

  @Test(expected = InvalidDataAccessApiUsageException.class)
  public void testCreateWithDuplicateNameFails() throws Exception {
    Account user = em.find(Account.class, 1);
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "intervention 1", "newMotivation", "http://semantic.com", "labelnew");
    interventionRepository.create(user, interventionCommand);
  }


}
