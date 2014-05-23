package org.drugis.addis.models.repositories.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.repositories.ModelRepository;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.springframework.stereotype.Repository;

/**
 * Created by connor on 23-5-14.
 */
@Repository
public class JpaModelRepository implements ModelRepository {

  @Override
  public Model create(Integer projectId, Integer analysisId, NetworkMetaAnalysisProblem problem) {
    return null;
  }
}
