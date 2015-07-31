package org.drugis.addis.patavitask.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.model.PairwiseNetworkProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by connor on 26-6-14.
 */
@Service
public class PataviTaskServiceImpl implements PataviTaskService {
  final static Logger logger = LoggerFactory.getLogger(PataviTaskServiceImpl.class);
  public final static String PATAVI_URI_BASE = System.getenv("PATAVI_URI");

  @Inject
  ModelRepository modelRepository;

  @Inject
  PataviTaskRepository pataviTaskRepository;

  @Inject
  ProblemService problemService;

  @Override
  public PataviTaskUriHolder getPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelTypeException {
    logger.trace("PataviTaskServiceImpl.getPataviTaskUriHolder, projectId = " + projectId + " analysisId = " + analysisId + "modelId = " + modelId);
    Model model = modelRepository.find(modelId);
    if(model == null) {
      throw new ResourceDoesNotExistException();
    }

    Integer pataviTaskId = model.getTaskId();
    if(pataviTaskId == null) {
      NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

      PataviTask pataviTask = null;
      if(Model.PAIRWISE_MODEL_TYPE.equals(model.getModelTypeTypeAsString())) {
        PairwiseNetworkProblem  pairwiseProblem = new PairwiseNetworkProblem(problem, model.getPairwiseDetails());
        pataviTask = pataviTaskRepository.createPataviTask(pairwiseProblem, model);
      } else if (Model.NETWORK_MODEL_TYPE.equals(model.getModelTypeTypeAsString())
              || Model.NODE_SPLITTING_MODEL_TYPE.equals(model.getModelTypeTypeAsString())) {
        pataviTask = pataviTaskRepository.createPataviTask(problem, model);
      } else {
        throw new InvalidModelTypeException("Invalid model type");
      }

      pataviTaskId = pataviTask.getId();
      model.setTaskId(pataviTaskId);
    }

    logger.debug("PATAVI_URI_BASE: " + PATAVI_URI_BASE);
    return new PataviTaskUriHolder(PATAVI_URI_BASE + pataviTaskId);
  }
}
