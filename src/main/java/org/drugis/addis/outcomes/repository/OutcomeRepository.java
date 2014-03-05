package org.drugis.addis.outcomes.repository;

import org.drugis.addis.outcomes.Outcome;

import java.util.Collection;

/**
 * Created by daan on 3/5/14.
 */
public interface OutcomeRepository {
  Collection<Outcome> query(Integer projectId);
}
