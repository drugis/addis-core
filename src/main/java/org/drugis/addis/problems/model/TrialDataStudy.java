package org.drugis.addis.problems.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataStudy {
  private String studyUid;
  private String name;
  private List<TrialDataIntervention> trialDataInterventions = new ArrayList<>();
  private List<TrialDataArm> trialDataArms = new ArrayList<>();

  public TrialDataStudy() {
  }

  public TrialDataStudy(String studyUid, String name, List<TrialDataIntervention> trialDataInterventions, List<TrialDataArm> trialDataArms) {
    this.studyUid = studyUid;
    this.name = name;

    if (trialDataInterventions != null) {
      this.trialDataInterventions = trialDataInterventions;
    }

    if (trialDataArms != null) {
      this.trialDataArms = trialDataArms;
    }
  }

  public String getStudyUid() {
    return studyUid;
  }

  public String getName() {
    return name;
  }

  public List<TrialDataIntervention> getTrialDataInterventions() {
    return trialDataInterventions;
  }

  public List<TrialDataArm> getTrialDataArms() {
    return trialDataArms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataStudy)) return false;

    TrialDataStudy that = (TrialDataStudy) o;

    if (!name.equals(that.name)) return false;
    if (!studyUid.equals(that.studyUid)) return false;
    if (!trialDataArms.equals(that.trialDataArms)) return false;
    if (!trialDataInterventions.equals(that.trialDataInterventions)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyUid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + trialDataInterventions.hashCode();
    result = 31 * result + trialDataArms.hashCode();
    return result;
  }
}