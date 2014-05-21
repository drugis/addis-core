package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {
  public List<ObjectNode> getVariablesByIds(Collection<Long> outcomeIds);

  public List<ObjectNode> getArmsByDrugIds(Integer studyId, Collection<Long> drugIds);

  public List<ObjectNode> getOrderedMeasurements(Collection<Long> outcomeIds, Collection<Long> armIds);

  public List<ObjectNode> getStudiesByIds(Long namespaceId, List<Long> studyIds);

  public ObjectNode getTrialData(Long namespaceId, String semanticOutcomeUri, List<String> alternativeUris);
}
