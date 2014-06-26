package org.drugis.addis.interventions.repository.impl;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.InterventionCommand;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by daan on 3/7/14.
 */
@Repository
public class InterventionRepositoryImpl implements org.drugis.addis.interventions.repository.InterventionRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public List<Intervention> query(Integer projectId) {
    TypedQuery<Intervention> query = em.createQuery("FROM Intervention i where i.project = :projectId", Intervention.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();

  }

  @Override
  public Intervention get(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException {
    TypedQuery<Intervention> query = em.createQuery("FROM Intervention i WHERE i.id = :interventionId AND i.project = :projectId", Intervention.class);
    query.setParameter("interventionId", interventionId);
    query.setParameter("projectId", projectId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Intervention create(Account user, InterventionCommand interventionCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Intervention newIntervention = new Intervention(interventionCommand.getProjectId(), interventionCommand.getName(), interventionCommand.getMotivation(), interventionCommand.getSemanticIntervention());
    Project project = em.find(Project.class, newIntervention.getProject());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId() != user.getId()) {
      throw new MethodNotAllowedException();
    }
    em.persist(newIntervention);
    return newIntervention;
  }
}
