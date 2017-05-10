package org.drugis.addis.scenarios.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.service.ScenarioService;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by connor on 16-4-14.
 */
@Service
public class ScenarioServiceImpl implements ScenarioService {

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private SubProblemRepository subProblemRepository;

  @Override
  public void checkCoordinates(Integer projectId, Integer analysisId, Integer subProblemId, Scenario scenario) throws ResourceDoesNotExistException {
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    SubProblem subProblem = subProblemRepository.get(subProblemId);

    if (!analysis.getProjectId().equals(projectId) ||
            !subProblem.getWorkspaceId().equals(analysisId) ||
            !scenario.getSubProblemId().equals(subProblemId)) {
      throw new ResourceDoesNotExistException();
    }
  }
}
