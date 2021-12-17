package org.drugis.addis.subProblems.repository.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * Created by joris on 8-5-17.
 */
@Repository
public class SubProblemRepositoryImpl implements SubProblemRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public SubProblem create(Integer workspaceId, String definition, String title) {
    SubProblem newSubProblem = new SubProblem(workspaceId, definition, title);
    em.persist(newSubProblem);
    return newSubProblem;
  }

  @Override
  public Collection<SubProblem> queryByProject(Integer projectId) {
    TypedQuery<SubProblem> query = em.createQuery(
            "SELECT DISTINCT sp FROM SubProblem sp\n" +
                    " WHERE sp.workspaceId in(\n " +
                    "    SELECT id FROM AbstractAnalysis where projectid = :projectId\n" +
                    "  )", SubProblem.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Collection<SubProblem> queryByProjectAndAnalysis(Integer projectId, Integer workspaceId) {
    TypedQuery<SubProblem> query = em.createQuery(
            "SELECT DISTINCT sp FROM SubProblem sp\n" +
                    " WHERE sp.workspaceId = :workspaceId \n" +
                    " AND sp.workspaceId in(\n " +
                    "    SELECT id FROM AbstractAnalysis where id = :workspaceId and projectid = :projectId\n" +
                    "  )", SubProblem.class);
    query.setParameter("workspaceId", workspaceId);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public SubProblem get(Integer subProblemId) throws ResourceDoesNotExistException {
    SubProblem subProblem = em.find(SubProblem.class, subProblemId);
    if (subProblem == null) {
      throw new ResourceDoesNotExistException();
    }
    return subProblem;
  }

  @Override
  public void update(Integer analysisId, Integer subProblemId, String definition, String title) throws ResourceDoesNotExistException {
    SubProblem subProblem = get(subProblemId);
    if (definition != null) {
      subProblem.setDefinition(definition);
    }
    if (title != null) {
      subProblem.setTitle(title);
    }

    em.merge(subProblem);
  }
}
