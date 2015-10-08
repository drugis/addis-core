package org.drugis.addis.models.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.*;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.hibernate.cfg.NotYetImplementedException;
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
  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException {
    return modelRepository.find(modelId);
  }

  @Override
  public List<Model> query(Integer analysisId) throws SQLException {
    return modelRepository.findByAnalysis(analysisId);
  }

  @Override
  public void checkOwnership(Integer modelId, Principal principal) {
    throw new NotYetImplementedException();
  }

  @Override
  public void increaseRunLength(UpdateModelCommand updateModelCommand) {
    throw new NotYetImplementedException();
  }
}
