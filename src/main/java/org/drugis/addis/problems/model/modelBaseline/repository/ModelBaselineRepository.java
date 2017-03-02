package org.drugis.addis.problems.model.modelBaseline.repository;

import org.drugis.addis.problems.model.modelBaseline.ModelBaseline;

/**
 * Created by joris on 2-3-17.
 */
public interface ModelBaselineRepository {
  ModelBaseline getModelBaseline(Integer modelId);
}
