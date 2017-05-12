package org.drugis.addis.subProblems.service;

import org.drugis.addis.subProblems.SubProblem;

/**
 * Created by joris on 8-5-17.
 */
public interface SubProblemService {
  void createMCDADefaults(Integer projectId, Integer analysisId, String scenarioState);

  SubProblem createSubProblem(Integer analysisId, String definition, String title, String scenarioState);
}
