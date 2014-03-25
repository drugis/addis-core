package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;

import java.util.List;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  public List<SemanticOutcome> getOutcomes(Long namespaceId);

  public List<SemanticIntervention> getInterventions(Long namespaceId);

  public List<Integer> getTrialverseDrugIds(Integer namespaceId, Integer studyId, List<String> interventionURIs);

  public List<Integer> getTrialverseOutcomeIds(Integer namespaceId, Integer studyId, List<String> outcomeURIs);
}
