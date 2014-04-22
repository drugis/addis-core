package org.drugis.addis.scenarios.repository;

import org.drugis.addis.analyses.State;
import org.drugis.addis.scenarios.Scenario;

import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
public interface ScenarioRepository {
  Scenario get(Integer id);

  Scenario create(Integer analysisId, String title, State state);

  Collection<Scenario> query(Integer projectId, Integer analysisId);

  /**
   * Only title and state are mutable.
   *
   * @param state A JSON string
   */
  Scenario update(Integer id, String title, String state);
}
