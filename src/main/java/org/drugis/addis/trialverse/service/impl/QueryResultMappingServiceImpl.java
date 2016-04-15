package org.drugis.addis.trialverse.service.impl;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.drugis.addis.trialverse.TrialverseUtilService.readValue;

/**
 * Created by connor on 8-4-16.
 */
@Service
public class QueryResultMappingServiceImpl implements QueryResultMappingService {

  public final static String SIMPLE_INTERVENTION_TYPE = "http://trials.drugis.org/ontology#SimpleDrugTreatment";
  public final static String FIXED_INTERVENTION_TYPE = "http://trials.drugis.org/ontology#FixedDoseDrugTreatment";
  public final static String TITRATED_INTERVENTION_TYPE = "http://trials.drugis.org/ontology#TitratedDoseDrugTreatment";

  @Override
  public Map<URI, TrialDataStudy> mapResultRowToTrialDataStudy(JSONArray bindings) throws ReadValueException {
    Map<URI, TrialDataStudy> trialDataStudies = new HashMap<>();
    for (Object binding : bindings) {
      JSONObject row = (JSONObject) binding;
      URI studyUri = readValue(row, "graph");
      TrialDataStudy trialDataStudy = trialDataStudies.get(studyUri);
      if (trialDataStudy == null) {
        String studyName = readValue(row, "studyName");
        trialDataStudy = new TrialDataStudy(studyUri, studyName, new ArrayList<>());
        trialDataStudies.put(studyUri, trialDataStudy);
      }

      URI drugInstance = readValue(row, "drugInstance");
      URI drugConcept = readValue(row, "drug");
      AbstractSemanticIntervention abstractSemanticIntervention;
      switch (readValue(row, "treatmentType").toString()){
        case FIXED_INTERVENTION_TYPE:
          Double fixedDoseValue = readValue(row, "fixedDoseValue");
          String fixedDoseDosingPeriodicity = readValue(row, "fixedDoseDosingPeriodicity");
          URI fixedDoseUnitConceptUri = readValue(row, "fixedDoseUnitConcept");
          String fixedDoseUnitLabel = readValue(row, "fixedDoseUnitLabel");
          Double fixedDoseUnitMultiplier = readValue(row, "fixedDoseUnitMultiplier");
          Dose fixedDose = new Dose(fixedDoseValue, fixedDoseDosingPeriodicity, fixedDoseUnitConceptUri, fixedDoseUnitLabel, fixedDoseUnitMultiplier);
          abstractSemanticIntervention = new FixedSemanticIntervention(drugInstance, drugConcept, fixedDose);
          break;
        case TITRATED_INTERVENTION_TYPE:
          Double maxDoseValue = readValue(row, "maxDoseValue");
          String maxDoseDosingPeriodicity = readValue(row, "maxDoseDosingPeriodicity");
          URI maxDoseUnitConceptUri = readValue(row, "maxDoseUnitConcept");
          String maxDoseUnitLabel = readValue(row, "maxDoseUnitLabel");
          Double maxDoseUnitMultiplier = readValue(row, "maxDoseUnitMultiplier");
          Dose maxDose = new Dose(maxDoseValue, maxDoseDosingPeriodicity, maxDoseUnitConceptUri,maxDoseUnitLabel, maxDoseUnitMultiplier);
          Double minDoseValue = readValue(row, "minDoseValue");
          String minDoseDosingPeriodicity = readValue(row, "minDoseDosingPeriodicity");
          URI minDoseUnitConceptUri = readValue(row, "minDoseUnitConcept");
          String minDoseUnitLabel = readValue(row, "minDoseUnitLabel");
          Double minDoseUnitMultiplier = readValue(row, "minDoseUnitMultiplier");
          Dose mixDose = new Dose(minDoseValue, minDoseDosingPeriodicity, minDoseUnitConceptUri ,minDoseUnitLabel, minDoseUnitMultiplier);
          abstractSemanticIntervention = new TitratedSemanticIntervention(drugInstance, drugConcept, mixDose, maxDose);
          break;
        default:
          abstractSemanticIntervention = new SimpleSemanticIntervention(drugInstance, drugConcept);
      }

      Double mean = null;
      Double stdDev = null;
      Integer rate = null;
      Boolean isContinuous = row.containsKey("mean");
      if (isContinuous) {
        mean =readValue(row, "mean");
        stdDev = readValue(row, "stdDev");
      } else {
        rate = readValue(row, "count");
      }
      Integer sampleSize = readValue(row, "sampleSize");
      URI armUri = readValue(row, "arm");
      String armLabel = readValue(row, "armLabel");
      URI variableUri = readValue(row, "outcomeInstance");
      Measurement measurement = new Measurement(studyUri, variableUri, armUri, sampleSize, rate, stdDev, mean);
      TrialDataArm trialDataArm = new TrialDataArm(armUri, armLabel, drugInstance, measurement, abstractSemanticIntervention);
      trialDataStudy.getTrialDataArms().add(trialDataArm);

    }
    return trialDataStudies;
  }

  @Override
  public CovariateStudyValue mapResultToCovariateStudyValue(JSONObject row) throws ReadValueException {
    URI studyUri = readValue(row, "graph");
    String populationCharacteristicCovariateKey = readValue(row, "populationCharacteristicCovariateKey");
    Double value = readValue(row, "value");
    return new CovariateStudyValue(studyUri, populationCharacteristicCovariateKey, value);
  }
}
