package org.drugis.addis.models.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.controller.command.*;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class ModelServiceImpl implements ModelService {
  @Inject
  ModelRepository modelRepository;

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  ProjectService projectService;

  @Inject
  PataviTaskRepository pataviTaskRepository;

  @Override

  public Model createModel(Integer analysisId, CreateModelCommand command) throws ResourceDoesNotExistException, InvalidModelException {
    ModelTypeCommand modelTypeCommand = command.getModelType();
    HeterogeneityPriorCommand heterogeneityPrior = command.getHeterogeneityPrior();
    String heterogeneityPriorType = determineHeterogeneityPriorType(heterogeneityPrior);
    Model.ModelBuilder builder = new Model.ModelBuilder(analysisId, command.getTitle())
            .linearModel(command.getLinearModel())
            .modelType(modelTypeCommand.getType())
            .heterogeneityPriorType(heterogeneityPriorType)
            .burnInIterations(command.getBurnInIterations())
            .inferenceIterations(command.getInferenceIterations())
            .thinningFactor(command.getThinningFactor())
            .likelihood(command.getLikelihood())
            .link(command.getLink())
            .outcomeScale(command.getOutcomeScale())
            .regressor(command.getRegressor())
            .sensitivity(command.getSensitivity());

    if (Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      StdDevValuesCommand heterogeneityValuesCommand = ((StdDevHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .lower(heterogeneityValuesCommand.getLower())
              .upper(heterogeneityValuesCommand.getUpper());
    } else if (Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      VarianceValuesCommand heterogeneityValuesCommand = ((VarianceHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .mean(heterogeneityValuesCommand.getMean())
              .stdDev(heterogeneityValuesCommand.getStdDev());
    } else if (Model.PRECISION_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPriorType)) {
      PrecisionValuesCommand heterogeneityValuesCommand = ((PrecisionHeterogeneityPriorCommand) heterogeneityPrior).getValues();
      builder = builder
              .rate(heterogeneityValuesCommand.getRate())
              .shape(heterogeneityValuesCommand.getShape());
    }

    DetailsCommand details = modelTypeCommand.getDetails();
    if (details != null) {
      builder = builder
              .from(new Model.DetailNode(details.getFrom().getId(), details.getFrom().getName()))
              .to(new Model.DetailNode(details.getTo().getId(), details.getTo().getName()));
    }

    Model model = builder.build();
    return modelRepository.persist(model);
  }

  private String determineHeterogeneityPriorType(HeterogeneityPriorCommand heterogeneityPriorCommand) {
    if (heterogeneityPriorCommand instanceof StdDevHeterogeneityPriorCommand) {
      return Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE;
    } else if (heterogeneityPriorCommand instanceof VarianceHeterogeneityPriorCommand) {
      return Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE;
    } else if (heterogeneityPriorCommand instanceof PrecisionHeterogeneityPriorCommand) {
      return Model.PRECISION_HETEROGENEITY_PRIOR_TYPE;
    } else {
      return null;
    }
  }

  @Override
  public List<Model> query(Integer analysisId) throws SQLException, IOException {
    return addRunStatusToModels(modelRepository.findByAnalysis(analysisId));
  }

  @Override
  public void checkOwnership(Integer modelId, Principal principal) throws ResourceDoesNotExistException, MethodNotAllowedException, IOException {
    Model model = modelRepository.get(modelId);
    AbstractAnalysis analysis = analysisRepository.get(model.getAnalysisId());

    projectService.checkOwnership(analysis.getProjectId(), principal);
  }

  private void checkIncrease(Model persistentModel, UpdateModelCommand updateModelCommand) throws MethodNotAllowedException {
    if (persistentModel.getBurnInIterations() > updateModelCommand.getBurnInIterations() ||
            persistentModel.getInferenceIterations() > updateModelCommand.getInferenceIterations()) {
      throw new MethodNotAllowedException();
    }
  }

  @Override
  public void increaseRunLength(UpdateModelCommand updateModelCommand) throws MethodNotAllowedException, InvalidModelException, IOException {
    Model oldModel = modelRepository.get(updateModelCommand.getId());

    // check that increase is not a decrease
    checkIncrease(oldModel, updateModelCommand);

    oldModel.setBurnInIterations(updateModelCommand.getBurnInIterations());
    oldModel.setInferenceIterations(updateModelCommand.getInferenceIterations());
    oldModel.setThinningFactor(updateModelCommand.getThinningFactor());
    pataviTaskRepository.delete(oldModel.getTaskUrl());
    oldModel.setTaskUrl(null);

    modelRepository.persist(oldModel);
  }

  @Override
  public List<Model> queryConsistencyModels(Integer projectId) throws SQLException, IOException {
    List<Model> consistencyModels = modelRepository
            .findNetworkModelsByProject(projectId)
            .stream()
            .filter(m -> m.getModelType().getType().equals(Model.NETWORK_MODEL_TYPE) ||
                    m.getModelType().getType().equals(Model.PAIRWISE_MODEL_TYPE))
            .collect(Collectors.toList());
    return addRunStatusToModels(consistencyModels);
  }

  @Override
  public List<Model> findByAnalysis(Integer analysisId) throws SQLException, IOException {
    return addRunStatusToModels(modelRepository.findByAnalysis(analysisId));
  }

  @Override
  public List<Model> findNetworkModelsByProject(Integer projectId) throws SQLException, IOException {
    return addRunStatusToModels(modelRepository.findNetworkModelsByProject(projectId));
  }

  @Override
  public List<Model> get(List<Integer> modelIds) throws SQLException, IOException {
    return addRunStatusToModels(modelRepository.get(modelIds));
  }

  @Override
  public Model find(Integer modelId) throws IOException, SQLException {
    return addRunStatusToModels(Collections.singletonList(modelRepository.find(modelId))).get(0);
  }

  @Override
  public Model get(Integer modelId) throws IOException, SQLException {
    return addRunStatusToModels(Collections.singletonList(modelRepository.get(modelId))).get(0);
  }

  private List<Model> addRunStatusToModels(List<Model> models) throws SQLException, IOException {
    List<URI> taskUrls = models.stream()
            .filter(model -> model.getTaskUrl() != null)
            .map(Model::getTaskUrl).collect(Collectors.toList());
    Map<URI, PataviTask> tasksByUri = pataviTaskRepository.findByUrls(taskUrls)
            .stream()
            .collect(Collectors.toMap(PataviTask::getSelf, Function.identity()));

    return models.stream()
            .map(model -> {
              URI taskUrl = model.getTaskUrl();
              if (taskUrl != null) {
                PataviTask pataviTask = tasksByUri.get(taskUrl);
                boolean isTaskRun = pataviTask.getResults() != null;
                return setHasRunStatus(model, isTaskRun);
              }
              return model;
            })
            .collect(Collectors.toList());
  }

  private Model setHasRunStatus(Model model, Boolean isTaskRun) {
    if (model.getTaskUrl() != null) {
      if (isTaskRun) {
        model.setHasResult();
      }
    }
    return model;
  }
}
