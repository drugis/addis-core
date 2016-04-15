package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.drugis.addis.trialverse.model.CovariateStudyValue;
import org.drugis.addis.trialverse.model.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;

import java.net.URI;
import java.util.Map;

/**
 * Created by connor on 8-4-16.
 */
public interface QueryResultMappingService {
  Map<URI, TrialDataStudy> mapResultRowToTrialDataStudy(JSONArray bindings) throws ReadValueException;
  CovariateStudyValue mapResultToCovariateStudyValue(JSONObject row) throws ReadValueException;
  TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow mapSingleStudyDataRow(JSONObject row) throws ReadValueException;
}
