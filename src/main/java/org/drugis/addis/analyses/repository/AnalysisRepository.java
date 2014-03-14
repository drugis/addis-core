package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by connor on 3/11/14.
 */
public interface AnalysisRepository {
  Collection<Analysis> query(Integer projectId);

  Analysis create(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;

  Analysis get(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;;

  Analysis update(Account user, Integer analysisId, Analysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;
}
