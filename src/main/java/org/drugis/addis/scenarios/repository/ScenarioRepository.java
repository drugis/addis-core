package org.drugis.addis.scenarios.repository;

import org.drugis.addis.scenarios.Scenario;

import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
public interface ScenarioRepository {
  Scenario get(Integer id);

  Scenario create(Integer analysisId, Integer subProblemId, String title, String state);

  Collection<Scenario> queryByProject(Integer projectId);

  Collection<Scenario> queryByAnalysis(Integer projectId, Integer analysisId);

  Collection<Scenario> queryBySubProblem(Integer projectId, Integer analysisId, Integer subProblemId);

  Scenario update(Integer id, String title, String state);
}
