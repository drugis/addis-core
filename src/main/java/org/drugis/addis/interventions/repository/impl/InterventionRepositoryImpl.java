package org.drugis.addis.interventions.repository.impl;

import com.google.common.collect.Sets;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.AbstractInterventionCommand;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 3/7/14.
 */
@Repository
public class InterventionRepositoryImpl implements InterventionRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Set<AbstractIntervention> query(Integer projectId) {
    TypedQuery<AbstractIntervention> query = em.createQuery("FROM AbstractIntervention where project = :projectId", AbstractIntervention.class);
    query.setParameter("projectId", projectId);
    return Sets.newHashSet(query.getResultList());
  }

  @Override
  public List<SingleIntervention> querySingleInterventions(Integer projectId) {
    TypedQuery<SingleIntervention> query = em.createQuery("FROM SingleIntervention where project = :projectId", SingleIntervention.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public AbstractIntervention get(Integer interventionId) throws ResourceDoesNotExistException {
    return em.find(AbstractIntervention.class, interventionId);
  }

  @Override
  public AbstractIntervention get(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException {
    AbstractIntervention abstractIntervention = em.find(AbstractIntervention.class, interventionId);
    if(abstractIntervention == null || abstractIntervention.getProject().intValue() != projectId){
      throw new ResourceDoesNotExistException();
    }
    return abstractIntervention;
  }

  @Override
  public AbstractIntervention getByProjectIdAndName(Integer projectId, String name) {
    TypedQuery<AbstractIntervention> query = em.createQuery("FROM AbstractIntervention WHERE project = :projectId AND name = :name", AbstractIntervention.class);
    query.setParameter("projectId", projectId);
    query.setParameter("name", name);
    return query.getResultList().get(0);
  }

  @Override
  public AbstractIntervention create(Account user, AbstractInterventionCommand interventionCommand) throws MethodNotAllowedException, ResourceDoesNotExistException, InvalidConstraintException {
    AbstractIntervention newIntervention = interventionCommand.toIntervention();
    Project project = em.find(Project.class, newIntervention.getProject());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId().intValue() != user.getId().intValue()) {
      throw new MethodNotAllowedException();
    }
    TypedQuery<AbstractIntervention> query = em.createQuery("FROM AbstractIntervention i WHERE i.name = :interventionName AND i.project = :projectId", AbstractIntervention.class);
    query.setParameter("interventionName", interventionCommand.getName());
    query.setParameter("projectId", interventionCommand.getProjectId());
    List<AbstractIntervention> results = query.getResultList();
    if (results.size() > 0) {
      throw new IllegalArgumentException("Duplicate outcome name " + interventionCommand.getName());
    }
    em.persist(newIntervention);
    return newIntervention;
  }

  @Override
  public boolean isExistingInterventionName(Integer interventionId, String name) {
    TypedQuery<AbstractIntervention> query = em.createQuery("FROM AbstractIntervention ai " +
            "WHERE ai.id != :interventionId " +
            "AND ai.name LIKE :name " +
            "AND ai.project = (" +
            " SELECT ai2.project " +
            " FROM AbstractIntervention ai2 " +
            " WHERE ai2.id = :interventionId" +
            ")", AbstractIntervention.class);
    query.setParameter("interventionId", interventionId);
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }
}
