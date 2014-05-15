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

  public TrialDataStudy(Long studyId, String name) {
    this.studyId = studyId;
    this.name = name;
  }

  public TrialDataStudy(Study study) {
    this.studyId = study.getId();
    this.name = study.getName();
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

  public void setTrialDataInterventions(List<TrialDataIntervention> trialDataInterventions) {
    this.trialDataInterventions = trialDataInterventions;
  }

  public void setTrialDataArms(List<TrialDataArm> trialDataArms) {
    this.trialDataArms = trialDataArms != null ? trialDataArms : new ArrayList<TrialDataArm>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataStudy)) return false;

    TrialDataStudy that = (TrialDataStudy) o;

    if (!studyId.equals(that.studyId)) return false;
    if (!name.equals(that.name)) return false;
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

  public static List<TrialDataStudy> toTrailDataStudy(List<Study> studies) {
    List<TrialDataStudy> trialDataStudies = new ArrayList<>(studies.size());
    for (Study study : studies) {
      trialDataStudies.add(new TrialDataStudy(study));
    }
    return trialDataStudies;
  }
}