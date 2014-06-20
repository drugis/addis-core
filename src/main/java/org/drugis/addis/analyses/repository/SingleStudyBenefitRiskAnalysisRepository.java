package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by connor on 3/11/14.
 */
public interface SingleStudyBenefitRiskAnalysisRepository {
  Collection<SingleStudyBenefitRiskAnalysis> query(Integer projectId);

  SingleStudyBenefitRiskAnalysis create(AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;

  SingleStudyBenefitRiskAnalysis update(Account user, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;
}
