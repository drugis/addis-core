package org.drugis.addis.outcomes.repository.impl;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 3/7/14.
 */
@Repository
public class OutcomeRepositoryImpl implements OutcomeRepository {

  final static Logger logger = LoggerFactory.getLogger(OutcomeRepositoryImpl.class);

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<Outcome> query(Integer projectId) {
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o where o.project = :projectId", Outcome.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Outcome get(Integer outcomeId) throws ResourceDoesNotExistException {
    return em.find(Outcome.class, outcomeId);
  }

  @Override
  public Outcome get(Integer projectId, Integer outcomeId) throws ResourceDoesNotExistException {
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o WHERE o.id = :outcomeId AND o.project = :projectId", Outcome.class);
    query.setParameter("outcomeId", outcomeId);
    query.setParameter("projectId", projectId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Boolean isExistingOutcomeName(Integer outcomeId, String name) {
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o " +
            "WHERE o.id != :outcomeId " +
            "AND o.name LIKE :name " +
            "AND o.project = (" +
            " SELECT o2.project " +
            " FROM Outcome o2 " +
            " WHERE o2.id = :outcomeId" +
            ")", Outcome.class);
    query.setParameter("outcomeId", outcomeId);
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }

  @Override
  public List<Outcome> get(Integer projectId, Set<Integer> outcomeIds) {
    if (outcomeIds.isEmpty()) {
      return Collections.emptyList();
    }
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o WHERE o.project = :projectId AND o.id IN :outcomeIds", Outcome.class);
    query.setParameter("projectId", projectId);
    query.setParameter("outcomeIds", outcomeIds);
    return query.getResultList();
  }

  @Override
  public Outcome create(Account user, Integer projectId, String name, Integer direction, String motivation, SemanticVariable semanticVariable) throws Exception {
    logger.trace("create outcome");
    Outcome newOutcome = new Outcome(projectId, name, direction, motivation, semanticVariable);
    Project project = em.find(Project.class, newOutcome.getProject());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId().intValue() != user.getId().intValue()) {
      logger.error("could not create outcome, for project: " + project.getId() + " where project.ownerId: " + project.getOwner().getId() + " and userID:  " + user.getId());
      throw new MethodNotAllowedException();
    }
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o WHERE o.name = :outcomeName AND o.project = :projectId", Outcome.class);
    query.setParameter("outcomeName", name);
    query.setParameter("projectId", projectId);
    List<Outcome> results = query.getResultList();
    if (results.size() > 0) {
      throw new Exception("Duplicate outcome name " + name);
    }
    em.persist(newOutcome);
    return newOutcome;
  }

  @Override
  public void delete(Integer outcomeId) throws ResourceDoesNotExistException {
    Outcome outcome = em.find(Outcome.class, outcomeId);
    if (outcome == null) {
      throw new ResourceDoesNotExistException("No outcome with id " + outcomeId);
    }
    em.remove(outcome);
  }
}
