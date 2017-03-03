package org.drugis.addis.models.repository;

import org.drugis.addis.models.ModelBaseline;

/**
 * Created by joris on 2-3-17.
 */
public interface ModelBaselineRepository {
  ModelBaseline getModelBaseline(Integer modelId);

  void setModelBaseline(Integer modelId, String baseline);
}
