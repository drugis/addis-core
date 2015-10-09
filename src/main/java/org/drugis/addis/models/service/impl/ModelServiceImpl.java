package org.drugis.addis.models.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.*;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

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
  public Model createModel(Integer analysisId, CreateModelCommand command) throws ResourceDoesNotExistException, InvalidModelTypeException {
    ModelTypeCommand modelTypeCommand = command.getModelType();
    Model.ModelBuilder modelBuilder = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(command.getTitle())
            .linearModel(command.getLinearModel())
            .modelType(modelTypeCommand.getType())
            .burnInIterations(command.getBurnInIterations())
            .inferenceIterations(command.getInferenceIterations())
            .thinningFactor(command.getThinningFactor())
            .likelihood(command.getLikelihood())
            .link(command.getLink())
            .outcomeScale(command.getOutcomeScale());

    DetailsCommand details = modelTypeCommand.getDetails();
    if (details != null) {
      modelBuilder = modelBuilder
              .from(new Model.DetailNode(details.getFrom().getId(), details.getFrom().getName()))
              .to(new Model.DetailNode(details.getTo().getId(), details.getTo().getName()));
    }

    Model model = modelBuilder.build();
    return modelRepository.persist(model);
  }

  @Override
  public List<Model> query(Integer analysisId) throws SQLException {
    return modelRepository.findByAnalysis(analysisId);
  }

  @Override
  public void checkOwnership(Integer modelId, Principal principal) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Model model = modelRepository.get(modelId);
    AbstractAnalysis analysis = analysisRepository.get(model.getAnalysisId());

    projectService.checkOwnership(analysis.getProjectId(), principal);
  }

  private void checkIncrease(Model persistendModel, UpdateModelCommand updateModelCommand) throws MethodNotAllowedException {
    if(persistendModel.getBurnInIterations() > updateModelCommand.getBurnInIterations() ||
            persistendModel.getInferenceIterations() > updateModelCommand.getInferenceIterations()) {
      throw new MethodNotAllowedException();
    }
  }

  @Override
  public void increaseRunLength(UpdateModelCommand updateModelCommand) throws MethodNotAllowedException, InvalidModelTypeException {
    Model oldModel = modelRepository.get(updateModelCommand.getId());

    // check that increase is not a decrease
    checkIncrease(oldModel, updateModelCommand);

    oldModel.setBurnInIterations(updateModelCommand.getBurnInIterations());
    oldModel.setInferenceIterations(updateModelCommand.getInferenceIterations());
    oldModel.setThinningFactor(updateModelCommand.getThinningFactor());
    pataviTaskRepository.delete(oldModel.getTaskId());
    oldModel.setTaskId(null);

    modelRepository.persist(oldModel);

  }
}
