package org.drugis.addis.trialverse.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 8-5-14.
 */
public class TrialData {

  private List<TrialDataStudy> trialDataStudies = new ArrayList<>();

  public TrialData(List<TrialDataStudy> trialDataStudies) {
    this.trialDataStudies = trialDataStudies;
  }

  public TrialData(Map<TrialDataStudy, List<Pair<Long, String>>> studiesWithInterventions) {

    for (TrialDataStudy trialDataStudy : studiesWithInterventions.keySet()) {

      List<Pair<Long, String>> drugsIdAndSemanticUris = studiesWithInterventions.get(trialDataStudy);
      List<TrialDataIntervention> interventions = new ArrayList<>(drugsIdAndSemanticUris.size());

      for (Pair<Long, String> drugAndSemanticUri : drugsIdAndSemanticUris) {
        interventions.add(new TrialDataIntervention(drugAndSemanticUri.getLeft(), drugAndSemanticUri.getRight()));
      }

      trialDataStudy.setTrialDataInterventions(interventions);
      this.trialDataStudies.add(trialDataStudy);
    }
  }

  public List<TrialDataStudy> getTrialDataStudies() {
    return trialDataStudies;
  }


}

