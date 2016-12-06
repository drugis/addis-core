package org.drugis.addis.covariates.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.covariates.CovariateService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by joris on 6-12-16.
 */
@Service
public class CovariateServiceImpl implements CovariateService {
  @Inject
  CovariateRepository covariateRepository;

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  ProjectService projectService;

  @Override
  public void delete(Account user, Integer projectId, Integer covariateId) throws ResourceDoesNotExistException, MethodNotAllowedException {
    List<AbstractAnalysis> analyses = analysisRepository.query(projectId);
    Boolean isCovariateUsed = analyses.stream()
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .anyMatch(analysis -> analysis.getCovariateInclusions().stream()
                    .anyMatch(inclusion -> inclusion.getCovariateId().equals(covariateId)));
    if (isCovariateUsed) {
      throw new OperationNotPermittedException("", "attempt to delete covariate that is in use");
    }
    projectService.checkProjectExistsAndModifiable(user, projectId);
    covariateRepository.delete(covariateId);
  }
}
