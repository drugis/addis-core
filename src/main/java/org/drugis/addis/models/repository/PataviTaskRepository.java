package org.drugis.addis.models.repository;

import org.drugis.addis.models.PataviTask;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {
  public PataviTask findPataviTask( Integer modelId);

  public PataviTask createPataviTask(Integer modelId, NetworkMetaAnalysisProblem problem);
}
