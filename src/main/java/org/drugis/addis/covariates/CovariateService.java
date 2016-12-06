package org.drugis.addis.covariates;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;

/**
 * Created by joris on 6-12-16.
 */
public interface CovariateService {
  void delete(Account user, Integer projectId, Integer covariateId) throws ResourceDoesNotExistException, MethodNotAllowedException;
}
