package org.drugis.addis.subProblems.repository;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.subProblems.SubProblem;

import java.util.Collection;

/**
 * Created by joris on 8-5-17.
 */
public interface SubProblemRepository {
  SubProblem create(Integer workspaceId, String definition, String title);

  Collection<SubProblem> queryByProject(Integer sourceProjectId);

  Collection<SubProblem> queryByProjectAndAnalysis(Integer projectId, Integer workspaceId);

  SubProblem get(Integer subProblemId) throws ResourceDoesNotExistException;

  void update(Integer analysisId, Integer subProblemId, String definition, String title) throws ResourceDoesNotExistException;
}
