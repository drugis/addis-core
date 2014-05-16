package org.drugis.addis.trialverse.service;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.model.TrialDataIntervention;

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
   * build a map of trialDataInterventions lists that is indexed by studyId
   */
  public Map<Long, List<TrialDataIntervention>> findStudyInterventions(Long namespaceId, List<Long> studyIds, List<String> interventionURIs);

  /**
   * Get a list for studyId-OutcomeVariable pairs that corresponds to the outcomeconcept is
   *
   * @param namespaceId
   * @param studyIds    The studies that should be included
   * @param outcomeURI  The outcome concept for which the trialverse variable ids should be resolved
   * @return
   */
  public List<Pair<Long, Long>> getOutcomeVariableIdsByStudyForSingleOutcome(Long namespaceId, List<Long> studyIds, String outcomeURI);
}
