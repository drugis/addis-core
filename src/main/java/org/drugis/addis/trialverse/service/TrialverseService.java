package org.drugis.addis.trialverse.service;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {
  public List<JSONObject> getVariablesByOutcomeIds(List<Integer> outcomeIds);
}
