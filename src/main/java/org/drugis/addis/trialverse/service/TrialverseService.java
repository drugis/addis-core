package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {
  public List<ObjectNode> getVariablesByIds(Set<String> outcomeIds);

  public List<ObjectNode> getArmsByDrugIds(String studyUid, Collection<String> drugUids);

  public List<ObjectNode> getOrderedMeasurements(List<String> outcomeIds, List<String> armIds);

  public ObjectNode getTrialData(String namespaceUId, String semanticOutcomeUri, List<String> alternativeUris);
}
