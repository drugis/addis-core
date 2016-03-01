package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.*;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface AnalysisService {
  void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;

  NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException;

  NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;

  SingleStudyBenefitRiskAnalysis createSingleStudyBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;

  void checkMetaBenefitRiskAnalysis(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  List<MbrOutcomeInclusion> buildInitialOutcomeInclusions(Integer projectId, Integer metabenefitRiskAnalysisId);
}
