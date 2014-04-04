package org.drugis.addis.scenarios.repository;

import org.drugis.addis.analyses.State;
import org.drugis.addis.scenarios.Scenario;

/**
 * Created by connor on 3-4-14.
 */
public interface ScenarioRepository {
  Scenario get(Integer id);

  Scenario create(Integer id, String title, State state);
}
