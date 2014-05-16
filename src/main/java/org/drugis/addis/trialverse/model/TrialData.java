package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 8-5-14.
 */
public class TrialData {

  private List<TrialDataStudy> trialDataStudies = new ArrayList<>();

  public TrialData(List<TrialDataStudy> trialDataStudies) {
    this.trialDataStudies = trialDataStudies;
  }

  public List<TrialDataStudy> getTrialDataStudies() {
    return trialDataStudies;
  }


}

