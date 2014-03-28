package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.trialverse.model.Arm;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {
  public List<ObjectNode> getVariablesByIds(Collection<Long> outcomeIds);

  public List<ObjectNode> getArmsByDrugIds(Integer studyId, Collection<Long> drugIds);

  public List<ObjectNode> getOrderedMeasurements(Integer studyId, Collection<Long> outcomeIds, Collection<Long> armIds);
}
