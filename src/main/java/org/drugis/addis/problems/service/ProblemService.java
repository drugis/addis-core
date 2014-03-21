package org.drugis.addis.problems.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.Problem;

/**
 * Created by daan on 3/21/14.
 */

public interface ProblemService {
  public Problem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;
}
