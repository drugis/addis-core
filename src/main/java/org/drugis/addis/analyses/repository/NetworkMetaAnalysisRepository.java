package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

/**
 * Created by connor on 6-5-14.
 */
public interface NetworkMetaAnalysisRepository {
  NetworkMetaAnalysis create(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;

}
