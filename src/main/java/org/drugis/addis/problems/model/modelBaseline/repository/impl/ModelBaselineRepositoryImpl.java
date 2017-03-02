package org.drugis.addis.problems.model.modelBaseline.repository.impl;

import org.drugis.addis.problems.model.modelBaseline.ModelBaseline;
import org.drugis.addis.problems.model.modelBaseline.repository.ModelBaselineRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Created by joris on 2-3-17.
 */
@Repository
public class ModelBaselineRepositoryImpl implements ModelBaselineRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public ModelBaseline getModelBaseline(Integer modelId) {
    TypedQuery<ModelBaseline> query =
              em.createQuery("FROM ModelBaseline WHERE modelId = :modelId", ModelBaseline.class);
    query.setParameter("modelId", modelId);
    return query.getResultList().get(0);
  }

  public void setBaseline(ModelBaseline modelBaseline) {
  }
}
