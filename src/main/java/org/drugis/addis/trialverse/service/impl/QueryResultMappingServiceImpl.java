package org.drugis.addis.trialverse.service.impl;

import com.jayway.jsonpath.JsonPath;
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

import static org.drugis.addis.trialverse.TrialverseUtilService.subStringAfterLastSymbol;

/**
 * Created by connor on 8-4-16.
 */
@Service
public class QueryResultMappingServiceImpl implements QueryResultMappingService {
  @Override
  public Map<String, TrialDataStudy> mapResultRowToTrialDataStudy(JSONArray bindings) {
    Map<String, TrialDataStudy> trialDataStudies = new HashMap<>();
    for (Object binding : bindings) {
      String studyUid = subStringAfterLastSymbol(JsonPath.read(binding, "$.graph.value"), '/');
      TrialDataStudy trialDataStudy = trialDataStudies.get(studyUid);
      if (trialDataStudy == null) {
        String studyName = JsonPath.read(binding, "$.studyName.value");
        trialDataStudy = new TrialDataStudy(studyUid, studyName, new ArrayList<>(), new ArrayList<>());
        trialDataStudies.put(studyUid, trialDataStudy);
      }
      String drugInstanceUid = subStringAfterLastSymbol(JsonPath.read(binding, "$.drugInstance.value"), '/');
      String drugUid = subStringAfterLastSymbol(JsonPath.read(binding, "$.drug.value"), '/');
      TrialDataIntervention trialDataIntervention = new TrialDataIntervention(drugInstanceUid, drugUid, studyUid);
      trialDataStudy.getTrialDataInterventions().add(trialDataIntervention);

      Double mean = null;
      Double stdDev = null;
      Long rate = null;
      JSONObject bindingObject = (JSONObject) binding;
      Boolean isContinuous = bindingObject.containsKey("mean");
      if (isContinuous) {
        mean = Double.parseDouble(JsonPath.read(binding, "$.mean.value"));
        stdDev = Double.parseDouble(JsonPath.read(binding, "$.stdDev.value"));
      } else {
        rate = Long.parseLong(JsonPath.read(binding, "$.count.value"));
      }
      Long sampleSize = Long.parseLong(JsonPath.read(binding, "$.sampleSize.value"));
      String armUid = subStringAfterLastSymbol(JsonPath.read(binding, "$.arm.value"), '/');
      String armLabel = JsonPath.read(binding, "$.armLabel.value");
      String variableUid = subStringAfterLastSymbol(JsonPath.read(binding, "$.outcomeInstance.value"), '/');
      Measurement measurement = new Measurement(studyUid, variableUid, armUid, sampleSize, rate, stdDev, mean);
      TrialDataArm trialDataArm = new TrialDataArm(armUid, armLabel, studyUid, drugInstanceUid, drugUid, measurement);
      trialDataStudy.getTrialDataArms().add(trialDataArm);
    }
    return trialDataStudies;
  }
}
