package org.drugis.addis.subProblems.service;

/**
 * Created by joris on 8-5-17.
 */
public interface SubProblemService {
  void createMCDADefaults(Integer projectId, Integer analysisId, String scenarioState);
}
