package org.drugis.addis.models.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.models.repositories.ModelRepository;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class ModelServiceImpl implements ModelService {
  @Inject
  ModelRepository modelRepository;
  @Inject
  ProblemService problemService;

  @Override
  public Model createModel(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) problemService.getProblem(projectId, analysisId);
    return modelRepository.create(projectId, analysisId, problem);  }
}
