package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.trialverse.model.Arm;

import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {
  public List<ObjectNode> getVariablesByOutcomeIds(List<Long> outcomeIds);

  public List<ObjectNode> getArmsByDrugIds(Integer studyId, List<Long> drugIds);

  public List<ObjectNode> getOrderedMeasurements(Integer studyId, List<Long> outcomeIds, List<Long> armIds);
}
