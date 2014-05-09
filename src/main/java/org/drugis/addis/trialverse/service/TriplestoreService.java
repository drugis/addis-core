package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  public List<SemanticOutcome> getOutcomes(Long namespaceId);

  public List<SemanticIntervention> getInterventions(Long namespaceId);

  public Map<Long, String> getTrialverseDrugs(Long namespaceId, Long studyId, Collection<String> interventionURIs);

  public Map<Long, String> getTrialverseVariables(Long namespaceId, Long studyId, Collection<String> outcomeURIs);

  public List<Long> findStudiesReferringToConcept(Long namespaceId, String conceptUri);
}
