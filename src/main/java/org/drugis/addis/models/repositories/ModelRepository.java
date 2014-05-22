package org.drugis.addis.models.repositories;

import org.drugis.addis.models.Model;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelRepository {

  Model create(Integer projectId, Integer analysisId, NetworkMetaAnalysisProblem problem);
}
