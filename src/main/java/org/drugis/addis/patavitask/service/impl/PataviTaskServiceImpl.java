package org.drugis.addis.patavitask.service.impl;

import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.model.PairwiseNetworkProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;

/**
 * Created by connor on 26-6-14.
 */
@Service
public class PataviTaskServiceImpl implements PataviTaskService {
  public final static String PATAVI_URI_BASE = System.getenv("PATAVI_URI");
  private final static Logger logger = LoggerFactory.getLogger(PataviTaskServiceImpl.class);
  @Inject
  private ModelService modelService;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private PataviTaskRepository pataviTaskRepository;

  @Inject
  private ProblemService problemService;

  @Inject
  private WebConstants webConstants;

  @Override
  public PataviTaskUriHolder getGemtcPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws Exception, ReadValueException, InvalidTypeForDoseCheckException, ProblemCreationException {
    logger.trace("PataviTaskServiceImpl.getGemtcPataviTaskUriHolder, projectId = " + projectId + " analysisId = " + analysisId + "modelId = " + modelId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    if(!(analysis instanceof NetworkMetaAnalysis)){
      throw new Exception("invalid  analysistype");
    }
    Integer preferredDirection = ((NetworkMetaAnalysis) analysis).getOutcome().getDirection();

    Model model = modelService.find(modelId);
    if(model == null) {
      throw new ResourceDoesNotExistException("Could not find model" + modelId);
    }

    URI pataviTaskUrl = model.getTaskUrl();
    if(pataviTaskUrl == null) {
      NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);

      NetworkMetaAnalysisProblem problemWithModelApplied = problemService.applyModelSettings(problem, model);

      if(Model.PAIRWISE_MODEL_TYPE.equals(model.getModelTypeTypeAsString())) {

        PairwiseNetworkProblem  pairwiseProblem = new PairwiseNetworkProblem(problemWithModelApplied, model.getPairwiseDetails());
        pataviTaskUrl = pataviTaskRepository.createPataviTask(webConstants.getPataviGemtcUri(), pairwiseProblem.buildProblemWithModelSettings(model, preferredDirection));

      } else if (Model.NETWORK_MODEL_TYPE.equals(model.getModelTypeTypeAsString())
              || Model.NODE_SPLITTING_MODEL_TYPE.equals(model.getModelTypeTypeAsString())
              || Model.REGRESSION_MODEL_TYPE.equals(model.getModelTypeTypeAsString())) {
        pataviTaskUrl = pataviTaskRepository.createPataviTask(webConstants.getPataviGemtcUri(), problemWithModelApplied.buildProblemWithModelSettings(model, preferredDirection));

      } else {
        throw new InvalidModelException("Invalid model type");
      }
      model.setTaskUrl(pataviTaskUrl);
    }

    return new PataviTaskUriHolder(pataviTaskUrl);
  }

  @Override
  public PataviTaskUriHolder getMcdaPataviTaskUriHolder(JSONObject problem) {
    URI createdUri = pataviTaskRepository.createPataviTask(webConstants.getPataviMcdaUri(), problem);
    return new PataviTaskUriHolder(createdUri);
  }
}
