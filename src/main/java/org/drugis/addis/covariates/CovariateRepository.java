package org.drugis.addis.covariates;

import java.util.Collection;

/**
 * Created by connor on 12/1/15.
 */
public interface CovariateRepository {
  Collection<Covariate> findByProject(Integer projectId);

  Covariate createForProject(Integer projectId, CovariateOption definition, String name, String motivation);
}
