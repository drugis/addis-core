package org.drugis.addis.covariates;

import org.drugis.addis.trialverse.model.emun.CovariateOptionType;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 12/1/15.
 */
public interface CovariateRepository {
  Collection<Covariate> findByProject(Integer projectId);

  Covariate createForProject(Integer projectId, String covariateDefinitionKey, String name, String motivation, CovariateOptionType type);

  List<Covariate> get(List<String> covariateKeys);
}
