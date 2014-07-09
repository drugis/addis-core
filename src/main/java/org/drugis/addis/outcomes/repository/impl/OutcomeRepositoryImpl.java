package org.drugis.addis.outcomes.repository.impl;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;

/**
 * Created by daan on 3/7/14.
 */
@Repository
public class OutcomeRepositoryImpl implements OutcomeRepository {

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
  public Outcome create(Account user, OutcomeCommand outcomeCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Outcome newOutcome = new Outcome(outcomeCommand.getProjectId(), outcomeCommand.getName(), outcomeCommand.getMotivation(), outcomeCommand.getSemanticOutcome());
    Project project = em.find(Project.class, newOutcome.getProject());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId() != user.getId()) {
      throw new MethodNotAllowedException();
    }
    TypedQuery<Outcome> query = em.createQuery("FROM Outcome o WHERE o.name = :outcomeName", Outcome.class);
    query.setParameter("outcomeName", outcomeCommand.getName());
    List<Outcome> results = query.getResultList();
    if (results.size() > 0) {
      throw new IllegalArgumentException("Duplicate outcome name " + outcomeCommand.getName());
    }
    em.persist(newOutcome);
    return newOutcome;
  }
}
