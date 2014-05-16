package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataStudy {
  private Long studyId;
  private String name;
  private List<TrialDataIntervention> trialDataInterventions = new ArrayList<>();
  private List<TrialDataArm> trialDataArms = new ArrayList<>();

  public TrialDataStudy(Long studyId, String name, List<TrialDataIntervention> trialDataInterventions, List<TrialDataArm> trialDataArms) {
    this.studyId = studyId;
    this.name = name;
    this.trialDataInterventions = trialDataInterventions;
    this.trialDataArms = trialDataArms;
  }

  public Long getStudyId() {
    return studyId;
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
    if (!studyId.equals(that.studyId)) return false;
    if (!trialDataArms.equals(that.trialDataArms)) return false;
    if (!trialDataInterventions.equals(that.trialDataInterventions)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + trialDataInterventions.hashCode();
    result = 31 * result + trialDataArms.hashCode();
    return result;
  }
}