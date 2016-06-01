package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.drugis.addis.trialverse.model.trialdata.CovariateStudyValue;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.net.URI;
import java.util.Map;

/**
 * Created by connor on 8-4-16.
 */
public interface QueryResultMappingService {
  Map<URI, TrialDataStudy> mapResultRowsToTrialDataStudy(JSONArray bindings) throws ReadValueException;
  CovariateStudyValue mapResultToCovariateStudyValue(JSONObject row) throws ReadValueException;
}
