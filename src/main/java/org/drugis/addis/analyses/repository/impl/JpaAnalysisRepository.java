package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by connor on 3/11/14.
 */
@Repository
public class JpaAnalysisRepository implements AnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<Analysis> query(Integer projectId) {
    TypedQuery<Analysis> query = em.createQuery("FROM Analysis a where a.projectId = :projectId", Analysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Analysis get(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    TypedQuery<Analysis> query = em.createQuery("FROM Analysis a WHERE a.id = :analysisId AND a.projectId = :projectId", Analysis.class);
    query.setParameter("analysisId", analysisId);
    query.setParameter("projectId", projectId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Analysis create(Account account, AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Analysis newAnalysis = new Analysis(analysisCommand.getProjectId(), analysisCommand.getName(), AnalysisType.getByLabel(analysisCommand.getType()), Collections.EMPTY_LIST);
    checkProjectExistsAndModifiable(account, analysisCommand);
    em.persist(newAnalysis);
    return newAnalysis;
  }

  @Override
  public Analysis update(Account user, Integer analysisId, Analysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Project project = em.find(Project.class, analysis.getProjectId());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId() != user.getId()) {
      throw new MethodNotAllowedException();
    }

    // do not allow changing of project ID
    Analysis oldAnalysis = em.find(Analysis.class, analysisId);
    if (!oldAnalysis.getProjectId().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }

    if (isNotEmpty(analysis.getSelectedOutcomes())) {
      // do not allow selection of outcomes that are not in the project
      for (Outcome outcome : analysis.getSelectedOutcomes()) {
        if (outcome.getProject() != analysis.getProjectId()) {
          throw new ResourceDoesNotExistException();
        }
      }

    }
    for (Outcome o : analysis.getSelectedOutcomes()) {
      oldAnalysis.addSelectedOutCome(o);
    }
    oldAnalysis.setName(analysis.getName());
    oldAnalysis.setStudy(analysis.getStudy());
    return em.merge(oldAnalysis);
  }

  private void checkProjectExistsAndModifiable(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Project project = em.find(Project.class, analysisCommand.getProjectId());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (project.getOwner().getId() != user.getId()) {
      throw new MethodNotAllowedException();
    }
  }

}
