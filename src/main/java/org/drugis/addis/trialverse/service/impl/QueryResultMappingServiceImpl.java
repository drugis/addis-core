package org.drugis.addis.trialverse.service.impl;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.drugis.addis.trialverse.model.Measurement;
import org.drugis.addis.trialverse.model.TrialDataArm;
import org.drugis.addis.trialverse.model.TrialDataIntervention;
import org.drugis.addis.trialverse.model.TrialDataStudy;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.drugis.addis.trialverse.TrialverseUtilService.readValue;
import static org.drugis.addis.trialverse.TrialverseUtilService.subStringAfterLastSymbol;

/**
 * Created by connor on 8-4-16.
 */
@Service
public class QueryResultMappingServiceImpl implements QueryResultMappingService {

  @Override
  public Map<String, TrialDataStudy> mapResultRowToTrialDataStudy(JSONArray bindings) throws ReadValueException {
    Map<String, TrialDataStudy> trialDataStudies = new HashMap<>();
    for (Object binding : bindings) {
      JSONObject row = (JSONObject) binding;
      String studyUid = subStringAfterLastSymbol(readValue(row, "graph"), '/');
      TrialDataStudy trialDataStudy = trialDataStudies.get(studyUid);
      if (trialDataStudy == null) {
        String studyName = readValue(row, "studyName");
        trialDataStudy = new TrialDataStudy(studyUid, studyName, new ArrayList<>(), new ArrayList<>());
        trialDataStudies.put(studyUid, trialDataStudy);
      }
      String drugInstanceUid = subStringAfterLastSymbol(readValue(row, "drugInstance"), '/');
      String drugUid = subStringAfterLastSymbol(readValue(row, "drug"), '/');
      TrialDataIntervention trialDataIntervention = new TrialDataIntervention(drugInstanceUid, drugUid, studyUid);
      trialDataStudy.getTrialDataInterventions().add(trialDataIntervention);

      Double mean = null;
      Double stdDev = null;
      Long rate = null;
      Boolean isContinuous = row.containsKey("mean");
      if (isContinuous) {
        mean =readValue(row, "mean");
        stdDev = readValue(row, "stdDev");
      } else {
        rate = readValue(row, "count");
      }
      Integer sampleSize = readValue(row, "sampleSize");
      String armUid = subStringAfterLastSymbol(readValue(row, "arm"), '/');
      String armLabel = readValue(row, "armLabel");
      String variableUid = subStringAfterLastSymbol(readValue(row, "outcomeInstance"), '/');
      Measurement measurement = new Measurement(studyUid, variableUid, armUid, sampleSize, rate, stdDev, mean);
      TrialDataArm trialDataArm = new TrialDataArm(armUid, armLabel, studyUid, drugInstanceUid, drugUid, measurement);
      trialDataStudy.getTrialDataArms().add(trialDataArm);

    }
    return trialDataStudies;
  }
}
