package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by connor on 23-5-14.
 */
@Repository
public class ModelRepositoryImpl implements ModelRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Model create(Integer analysisId, String modelTitle, String linearModel, String modelType, String from, String to,
                      Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) throws InvalidModelTypeException {
    Model model = new Model(analysisId, modelTitle, linearModel, modelType, from, to, burnInIterations, inferenceIterations, thinningFactor);
    em.persist(model);
    return model;
  }

  @Override
  public Model find(Integer modelId) {
    return em.find(Model.class, modelId);
  }

  @Override
  public List<Model> findByAnalysis(Integer networkMetaAnalysisId) {
    TypedQuery<Model> query = em.createQuery("FROM Model m WHERE m.analysisId = :analysisId", Model.class);
    query.setParameter("analysisId", networkMetaAnalysisId);
    return query.getResultList();
  }
}
