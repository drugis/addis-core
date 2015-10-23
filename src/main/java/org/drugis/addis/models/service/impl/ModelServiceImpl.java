package org.drugis.addis.models.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.*;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
  public Model createModel(Integer analysisId, ModelCommand command) throws ResourceDoesNotExistException, InvalidModelTypeException, InvalidHeterogeneityTypeException {
    ModelTypeCommand modelTypeCommand = command.getModelType();
    HeterogeneityPriorCommand heterogeneityPrior = command.getHeterogeneityPrior();
    Model.ModelBuilder builder = new Model.ModelBuilder()
        .analysisId(analysisId)
        .title(command.getTitle())
        .linearModel(command.getLinearModel())
        .modelType(modelTypeCommand.getType())
        .heterogeneityPriorType(heterogeneityPrior.getType())
        .burnInIterations(command.getBurnInIterations())
        .inferenceIterations(command.getInferenceIterations())
        .thinningFactor(command.getThinningFactor())
        .likelihood(command.getLikelihood())
        .link(command.getLink())
        .outcomeScale(command.getOutcomeScale());

    if (Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPrior.getType())) {
      StdDevValuesCommand heterogeneityValuesCommand = (StdDevValuesCommand) heterogeneityPrior.getHeterogeneityValuesCommand();
      builder = builder
          .lower(heterogeneityValuesCommand.getLower())
          .upper(heterogeneityValuesCommand.getUpper());
    } else if (Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPrior.getType())) {
      VarianceValuesCommand heterogeneityValuesCommand = (VarianceValuesCommand) heterogeneityPrior.getHeterogeneityValuesCommand();
      builder = builder
          .mean(heterogeneityValuesCommand.getMean())
          .stdDev(heterogeneityValuesCommand.getStdDev());

    } else if (Model.PRECISION_HETEROGENEITY_PRIOR_TYPE.equals(heterogeneityPrior.getType())) {
      PrecisionValuesCommand heterogeneityValuesCommand = (PrecisionValuesCommand) heterogeneityPrior.getHeterogeneityValuesCommand();
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

  @Override
  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException {
    return modelRepository.find(modelId);
  }

  @Override
  public List<Model> query(Integer analysisId) throws SQLException {
    return modelRepository.findByAnalysis(analysisId);
  }
}
