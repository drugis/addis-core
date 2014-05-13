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

  /**
   * Finds all studies that are part of the given namespace, have an id that is contained within the studyIds list
   * and have at least one of the given interventions
   *
   * @param namespaceId
   * @param studyIds
   * @param interventionURIs
   * @return a map from studyIds to drugIds
   */
  public Map<Long, List<Long>> findStudyInterventions(Long namespaceId, List<Long> studyIds, List<String> interventionURIs);
}
