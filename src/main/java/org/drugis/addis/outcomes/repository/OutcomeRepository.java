package org.drugis.addis.outcomes.repository;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticVariable;

import java.util.Collection;
import java.util.List;

/**
 * Created by daan on 3/7/14.
 */
public interface OutcomeRepository {
  Collection<Outcome> query(Integer projectId);

  Outcome get(Integer outcomeId) throws ResourceDoesNotExistException;

  Outcome get(Integer projectId, Integer outcomeId) throws ResourceDoesNotExistException;

  Outcome create(Account user, Integer projectId, String name, Integer direction, String motivation, SemanticVariable semanticVariable) throws MethodNotAllowedException, ResourceDoesNotExistException, Exception;

  List<Outcome> get(Integer projectId, List<Integer> outcomeIds);

  Boolean isExistingOutcomeName(Integer outcomeId, String name);

  void delete(Integer outcomeId) throws ResourceDoesNotExistException;
}
