package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by daan on 25-2-16.
 */
public interface MetaBenefitRiskAnalysisRepository {
  Collection<MetaBenefitRiskAnalysis> queryByProject(Integer projectId);

  MetaBenefitRiskAnalysis create(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;

  MetaBenefitRiskAnalysis update(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;
}
