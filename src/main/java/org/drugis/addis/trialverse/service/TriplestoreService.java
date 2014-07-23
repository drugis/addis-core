package org.drugis.addis.trialverse.service;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  public Collection<Namespace> queryNameSpaces();

  public Namespace getNamespace(String uid);

  public List<SemanticOutcome> getOutcomes(String namespaceUid);

  public List<SemanticIntervention> getInterventions(String namespaceUid);

  public List<Study> queryStudies(String namespaceUid);

  public Map<String, String> getTrialverseDrugs(String namespaceUid, String studyUid, Collection<String> interventionURIs);

  public Map<String, String> getTrialverseVariables(String namespaceUid, String studyId, Collection<String> outcomeURIs);

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
   * @param outcomeURI   The outcome concept for which the trialverse variable ids should be resolved
   * @return
   */
  public List<Pair<Long, Long>> getOutcomeVariableUidsByStudyForSingleOutcome(String namespaceUid, List<String> studyUids, String outcomeURI);

  public List<TrialDataStudy> getTrialData(String namespaceUid, String outcomeUri, List<String> interventionUris);
}
