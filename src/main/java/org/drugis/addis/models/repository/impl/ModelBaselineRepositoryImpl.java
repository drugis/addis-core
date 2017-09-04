package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.ModelBaseline;
import org.drugis.addis.models.repository.ModelBaselineRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by joris on 2-3-17.
 */
@Repository
public class ModelBaselineRepositoryImpl implements ModelBaselineRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public ModelBaseline getModelBaseline(Integer modelId) {
    return em.find(ModelBaseline.class, modelId);
  }

  @Override
  public void setModelBaseline(Integer modelId, String baseline) {
    ModelBaseline modelBaseline = em.find(ModelBaseline.class, modelId);
    if (modelBaseline != null) {
      modelBaseline.setBaseline(baseline);
    } else {
      em.persist(new ModelBaseline(modelId, baseline));
    }
  }
}
