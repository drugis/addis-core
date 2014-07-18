package org.drugis.addis.trialverse.service;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.Namespace;
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
  public Collection<Namespace> queryNameSpaces();

  public Namespace getNamespace(String uid);

  public List<SemanticOutcome> getOutcomes(Long namespaceId);

  public List<SemanticIntervention> getInterventions(Long namespaceId);

  public Map<Long, String> getTrialverseDrugs(String namespaceUid, Long studyId, Collection<String> interventionURIs);

  public Map<Long, String> getTrialverseVariables(String namespaceUid, Long studyId, Collection<String> outcomeURIs);

  public List<Long> findStudiesReferringToConcept(String namespaceUid, String conceptUri);

  /**
   * build a map of trialDataInterventions lists that is indexed by studyId
   */
  public Map<String, List<TrialDataIntervention>> findStudyInterventions(String namespaceUid, List<String> studyUds, List<String> interventionURIs);

  /**
   * Get a list for studyId-OutcomeVariable pairs that corresponds to the outcomeconcept is
   *
   * @param namespaceUid
   * @param studyUids    The studies that should be included
   * @param outcomeURI  The outcome concept for which the trialverse variable ids should be resolved
   * @return
   */
  public List<Pair<Long, Long>> getOutcomeVariableIdsByStudyForSingleOutcome(String namespaceUid, List<String> studyUids, String outcomeURI);

}
