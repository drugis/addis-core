package org.drugis.addis.outcomes.service;

import org.drugis.addis.outcomes.Outcome;

/**
 * Created by connor on 10-6-16.
 */
public interface OutcomeService {
  Outcome updateOutcome(Integer projectId, Integer outcomeId, String name, String motivation, Integer direction) throws Exception;
}
