package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

/**
 * Created by daan on 22-5-14.
 */
public interface AnalysisService {
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;

  public NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  public NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;

  public SingleStudyBenefitRiskAnalysis createSingleStudyBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;
}
