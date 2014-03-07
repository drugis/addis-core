package org.drugis.addis.outcomes.repository;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by daan on 3/7/14.
 */
public interface OutcomeRepository {
  Collection<Outcome> query(Integer projectId);

  Outcome get(Integer projectId, Integer outcomeId) throws ResourceDoesNotExistException;

  Outcome create(Account user, OutcomeCommand outcomeCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;
}
