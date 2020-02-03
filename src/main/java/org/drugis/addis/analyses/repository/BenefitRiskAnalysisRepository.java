package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.model.AnalysisCommand;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

public interface BenefitRiskAnalysisRepository {
  Collection<BenefitRiskAnalysis> queryByProject(Integer projectId);

  BenefitRiskAnalysis create(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException;

  BenefitRiskAnalysis update(Account user, BenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  BenefitRiskAnalysis find(Integer id);
}
