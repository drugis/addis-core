package org.drugis.addis.trialverse.service;

import org.apache.commons.lang3.tuple.Pair;
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
   * Finds all studies that:
   * - Are part of the given namespace,
   * - Have an id that is contained within the studyIds list
   * - Report on at least one of the given interventions
   *
   * @return a map from studyIds to pairs of drugId and drugUri
   */
  public Map<Long, List<Pair<Long, String>>> findStudyInterventions(Long namespaceId, List<Long> studyIds, List<String> interventionURIs);

  /**
   * Get a list for studyId-OutcomeVariable pairs that corresponds to the outcomeconcept is
   *
   * @param namespaceId
   * @param studyIds    The studies that should be included
   * @param outcomeURI  The outcome concept for which the trialverse variable ids should be resolved
   * @return
   */
  public List<Pair<Long, Long>> getOutComeVariableIdsByStudyForSingleOutcome(Long namespaceId, List<Long> studyIds, String outcomeURI);
}
