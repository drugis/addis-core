package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

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
  public Model persist(Model model) throws InvalidModelTypeException {
    em.persist(model);
    return model;
  }

  @Override
  public Model find(Integer modelId) {
    return em.find(Model.class, modelId);
  }

  @Override
  public List<Model> findByAnalysis(Integer networkMetaAnalysisId) throws SQLException {
    TypedQuery<Model> query = em.createQuery("FROM Model m WHERE m.analysisId = :analysisId", Model.class);
    query.setParameter("analysisId", networkMetaAnalysisId);
    List<Model> models = query.getResultList();
    List<Integer> modelIds = models.stream().map(Model::getId).collect(Collectors.toList());
    List<PataviTask> pataviTasks = pataviTaskRepository.findByIds(modelIds);

    //todo join tasks  this models to indicate hasRun on models;
    return models;
  }
}
