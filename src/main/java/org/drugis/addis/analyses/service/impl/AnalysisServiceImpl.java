package org.drugis.addis.analyses.service.impl;

import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

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

  @Inject
  OutcomeRepository outcomeRepository;

  @Override
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    if (!analysis.getProjectId().equals(projectId)) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    checkProjectIdChange(analysis);

    if (!modelRepository.findByAnalysis(analysis.getId()).isEmpty()) {
      // can not update locked exception
      throw new MethodNotAllowedException();
    }

    // do not allow selection of outcome that is not in the project
    if (analysis.getOutcome() != null && !analysis.getOutcome().getProject().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }

    for (ArmExclusion armExclusion : analysis.getExcludedArms()) {
      armExclusion.setAnalysis(analysis);
    }

    for (InterventionInclusion interventionInclusion : analysis.getIncludedInterventions()) {
      interventionInclusion.setAnalysis(analysis);
    }

    for (CovariateInclusion covariateInclusion : analysis.getCovariateInclusions()) {
      covariateInclusion.setAnalysis(analysis);
    }

    return networkMetaAnalysisRepository.update(analysis);
  }

  @Override
  public void checkMetaBenefitRiskAnalysis(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    checkProjectIdChange(analysis);
    if (isNotEmpty(analysis.getIncludedAlternatives())) {
      // do not allow selection of interventions that are not in the project
      for (Intervention intervention : analysis.getIncludedAlternatives()) {
        if (!intervention.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
    if (isNotEmpty(analysis.getMbrOutcomeInclusions())) {
      // do not allow selection of outcomes that are not in the project
      for (MbrOutcomeInclusion mbrOutcomeInclusion : analysis.getMbrOutcomeInclusions()) {
        Integer outcomeId = mbrOutcomeInclusion.getOutcomeId();
        Outcome outcome = outcomeRepository.get(outcomeId);
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
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

  private void checkProjectIdChange(AbstractAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    // do not allow changing of project ID
    AbstractAnalysis oldAnalysis = analysisRepository.get(analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }
  }


}
