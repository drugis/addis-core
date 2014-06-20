package org.drugis.addis.models.repositories.impl;

import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repositories.ModelRepository;
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
public class JpaModelRepository implements ModelRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Model create(Integer analysisId) {
    Model model = new Model(analysisId);
    em.persist(model);
    return model;
  }

  @Override
  public Model get(Integer modelId) {
    return em.find(Model.class, modelId);
  }

  @Override
  public Model findByAnalysis(NetworkMetaAnalysis networkMetaAnalysis) {
    TypedQuery<Model> query = em.createQuery("FROM Model m WHERE m.analysisId = :analysisId", Model.class);
    query.setParameter("analysisId", networkMetaAnalysis.getId());
    List<Model> resultList = query.getResultList();
    if (resultList.isEmpty()) {
      // no model found for given analysis
      return null;
    } else {
      return resultList.get(0);
    }
  }
}
