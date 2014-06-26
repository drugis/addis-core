package org.drugis.addis.outcomes.repository.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.PataviTask;
import org.drugis.addis.models.PataviTaskUriHolder;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.repository.PataviTaskRepository;
import org.drugis.addis.models.service.PataviTaskService;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by connor on 26-6-14.
 */
@Service
public class PataviTaskServiceImpl implements PataviTaskService {
  private final static String PATAVI_URI_BASE = System.getenv("PATAVI_URI");

  @Inject
  ModelRepository modelRepository;

  @Inject
  PataviTaskRepository pataviTaskRepository;

  @Inject
  ProblemService problemService;

  @Override
  public PataviTaskUriHolder getPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws ResourceDoesNotExistException {
    if(modelRepository.find(modelId) == null) {
      throw new ResourceDoesNotExistException();
    }
    PataviTask pataviTask = pataviTaskRepository.findPataviTask(modelId);
    if (pataviTask == null) {
      NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);
      pataviTask = pataviTaskRepository.createPataviTask(modelId, problem);
    }
    return new PataviTaskUriHolder(PATAVI_URI_BASE + pataviTask.getId());
  }
}
