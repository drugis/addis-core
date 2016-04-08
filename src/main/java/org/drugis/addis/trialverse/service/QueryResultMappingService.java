package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import org.drugis.addis.trialverse.model.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.util.Map;

/**
 * Created by connor on 8-4-16.
 */
public interface QueryResultMappingService {
  Map<String, TrialDataStudy> mapResultRowToTrialDataStudy(JSONArray bindings) throws ReadValueException;
}
