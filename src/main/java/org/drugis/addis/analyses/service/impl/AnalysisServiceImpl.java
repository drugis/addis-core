package org.drugis.addis.analyses.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.repositories.ModelRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Inject
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Inject
  ProjectService projectService;

  @Inject
  ModelRepository modelRepository;

  @Override
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    AbstractAnalysis analysis = analysisRepository.get(projectId, analysisId);
    if(!analysis.getProjectId().equals(projectId)) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisProjectId = analysis.getProjectId();
    projectService.checkProjectExistsAndModifiable(user, analysisProjectId);

    if (modelRepository.findByAnalysis(analysis) != null) {
      // can not update locked exception
      throw new MethodNotAllowedException();
    }

    // do not allow changing of project ID
    NetworkMetaAnalysis oldAnalysis = (NetworkMetaAnalysis) analysisRepository.get(analysis.getProjectId(), analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysisProjectId)) {
      throw new ResourceDoesNotExistException();
    }

    // do not allow selection of outcome that is not in the project
    if (analysis.getOutcome() != null && !analysis.getOutcome().getProject().equals(analysisProjectId)) {
      throw new ResourceDoesNotExistException();
    }

    return networkMetaAnalysisRepository.update(analysis);
  }

  @Override
  public NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return networkMetaAnalysisRepository.create(analysisCommand);
  }

  @Override
  public SingleStudyBenefitRiskAnalysis createSingleStudyBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return singleStudyBenefitRiskAnalysisRepository.create(analysisCommand);
  }
}
