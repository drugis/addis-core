package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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


  private Model setHasRunStatus(Model model, PataviTask pataviTask) {
      if (model.getTaskId() != null) {
        if (pataviTask != null && pataviTask.isHasResult()) {
          model.setHasResult();
        }
      }
      return model;
  }


  @Override
  public Model persist(Model model) throws InvalidModelException {
    em.persist(model);
    return model;
  }

  @Override
  public Model find(Integer modelId) {
    Model model = em.find(Model.class, modelId);
    if (model != null && model.getTaskId() != null) {
      PataviTask pataviTask = pataviTaskRepository.get(model.getTaskId());
      return setHasRunStatus(model, pataviTask);
    }

    return model;
  }

  @Override
  public Model get(Integer modelId) {
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
    List<Model> models = query.getResultList();

    return addTasksToModels(models);
  }

  @Override
  public List<Model> findNetworkModelsByProject(Integer projectId) throws SQLException {
    TypedQuery<Model> query = em.createQuery("SELECT m FROM Model m, NetworkMetaAnalysis a WHERE m.analysisId = a.id AND a.projectId = :projectId", Model.class);
    query.setParameter("projectId", projectId);
    List<Model> models = query.getResultList();

    return addTasksToModels(models);
  }

  private List<Model> addTasksToModels(List<Model> models) throws SQLException {
    List<Integer> taskIds = models.stream().map(Model::getTaskId).collect(Collectors.toList());
    List<PataviTask> pataviTasks = pataviTaskRepository.findByIds(taskIds);

    Map<Integer, PataviTask> taskMap = pataviTasks.stream()
            .collect(Collectors.toMap(PataviTask::getId, Function.identity()));

    return models.stream()
            .map(model -> setHasRunStatus(model, taskMap.get(model.getTaskId())))
            .collect(Collectors.toList());
  }
}
