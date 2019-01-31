package org.drugis.addis.trialverse.model;

import java.util.*;

public class Study {

  private String studyUuid;
  private String studyGraphUid;
  private String name;
  private String title;
  private List<String> outcomeUids = new ArrayList<>();
  private Set<StudyTreatmentArm> treatmentArms = new HashSet<>();

  public Study() {
  }

  public Study(String studyUuid, String studyGraphUid, String name, String title, List<String> outcomeUids) {
    this.studyUuid = studyUuid;
    this.studyGraphUid = studyGraphUid;
    this.name = name;
    this.title = title;
    this.outcomeUids = outcomeUids;
  }

  public String getStudyUid() {
    return studyUuid;
  }

  public String getStudyGraphUid() {
    return studyGraphUid;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public List<String> getOutcomeUids() {
    return outcomeUids;
  }

  public Set<StudyTreatmentArm> getTreatmentArms() {
    return treatmentArms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Study study = (Study) o;
    return Objects.equals(studyUuid, study.studyUuid) &&
            Objects.equals(studyGraphUid, study.studyGraphUid) &&
            Objects.equals(name, study.name) &&
            Objects.equals(title, study.title) &&
            Objects.equals(outcomeUids, study.outcomeUids) &&
            Objects.equals(treatmentArms, study.treatmentArms);
  }

  @Override
  public int hashCode() {

    return Objects.hash(studyUuid, studyGraphUid, name, title, outcomeUids, treatmentArms);
  }
}
