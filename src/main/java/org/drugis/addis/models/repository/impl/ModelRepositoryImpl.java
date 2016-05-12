package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created by connor on 23-5-14.
 */
@Repository
public class ModelRepositoryImpl implements ModelRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  PataviTaskRepository pataviTaskRepository;




  @Override
  public Model persist(Model model) throws InvalidModelException {
    em.persist(model);
    return model;
  }

  @Override
  public Model find(Integer modelId) throws IOException {
    return em.find(Model.class, modelId);
  }

  @Override
  public Model get(Integer modelId) throws IOException {
    Model model = find(modelId);
    if (model == null) {
      throw new ObjectRetrievalFailureException("model not found", modelId);
    }
    return model;
  }

  @Override
  public List<Model> get(List<Integer> modelIds) {
    if(modelIds.isEmpty()) {
      return Collections.emptyList();
    }

    TypedQuery<Model> query = em.createQuery("FROM Model WHERE id IN :modelIds", Model.class);
    query.setParameter("modelIds", modelIds);
    return query.getResultList();
  }

  @Override
  public List<Model> findByAnalysis(Integer networkMetaAnalysisId) throws SQLException {
    TypedQuery<Model> query = em.createQuery("FROM Model m WHERE m.analysisId = :analysisId", Model.class);
    query.setParameter("analysisId", networkMetaAnalysisId);
    return query.getResultList();
  }

  @Override
  public List<Model> findNetworkModelsByProject(Integer projectId) throws SQLException {
    TypedQuery<Model> query = em.createQuery("SELECT m FROM Model m, NetworkMetaAnalysis a WHERE m.analysisId = a.id AND a.projectId = :projectId", Model.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }
}
