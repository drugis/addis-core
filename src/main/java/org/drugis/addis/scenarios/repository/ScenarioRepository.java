package org.drugis.addis.scenarios.repository;

import org.drugis.addis.scenarios.Scenario;

import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
public interface ScenarioRepository {
  Scenario get(Integer id);

  Scenario create(Integer analysisId, String title, String state);

  Collection<Scenario> queryByProject(Integer projectId);

  Collection<Scenario> queryByProjectAndAnalysis(Integer projectId, Integer analysisId);

  Scenario update(Integer id, String title, String state);
}
