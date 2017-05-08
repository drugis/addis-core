package org.drugis.addis.subProblem.repository;

import org.drugis.addis.subProblem.SubProblem;

import java.util.Collection;

/**
 * Created by joris on 8-5-17.
 */
public interface SubProblemRepository {
  SubProblem create(Integer workspaceId, String definition, String title);

  Collection<SubProblem> queryByProject(Integer sourceProjectId);

  Collection<SubProblem> queryByProjectAndAnalysis(Integer projectId, Integer workspaceId);

  SubProblem get(Integer subProblemId);
}
