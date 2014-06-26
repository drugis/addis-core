package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.PataviTask;
import org.drugis.addis.models.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public PataviTask findPataviTask(Integer modelId) {
    TypedQuery<PataviTask> query = em.createQuery("from PataviTask pt where pt.modelId = :modelId  ", PataviTask.class);
    query.setParameter("modelId", modelId);
    List<PataviTask> resultList = query.getResultList();
    return resultList.size() > 0 ? resultList.get(0) : null;
  }

  @Override
  public PataviTask createPataviTask(Integer modelId, NetworkMetaAnalysisProblem problem) {
    PataviTask newPataviTask = new PataviTask(modelId, problem.toString());
    em.persist(newPataviTask);
    em.flush();
    return newPataviTask;
  }
}
