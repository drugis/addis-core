package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 3/19/14.
 */
public class Study {

  private String studyUid;
  private String studyGraphUid;
  private String name;
  private String title;
  private List<String> outcomeUids = new ArrayList<>();
  private Set<StudyTreatmentArm> treatmentArms = new HashSet<>();

  public Study() {
  }

  public Study(String studyUid, String studyGraphUid, String name, String title, List<String> outcomeUids) {
    this.studyUid = studyUid;
    this.studyGraphUid = studyGraphUid;
    this.name = name;
    this.title = title;
    this.outcomeUids = outcomeUids;
  }

  public String getStudyUid() {
    return studyUid;
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

    if (studyUid != null ? !studyUid.equals(study.studyUid) : study.studyUid != null) return false;
    if (studyGraphUid != null ? !studyGraphUid.equals(study.studyGraphUid) : study.studyGraphUid != null)
      return false;
    if (name != null ? !name.equals(study.name) : study.name != null) return false;
    if (title != null ? !title.equals(study.title) : study.title != null) return false;
    if (outcomeUids != null ? !outcomeUids.equals(study.outcomeUids) : study.outcomeUids != null) return false;
    return !(treatmentArms != null ? !treatmentArms.equals(study.treatmentArms) : study.treatmentArms != null);

  }

  @Override
  public int hashCode() {
    int result = studyUid != null ? studyUid.hashCode() : 0;
    result = 31 * result + (studyGraphUid != null ? studyGraphUid.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (outcomeUids != null ? outcomeUids.hashCode() : 0);
    result = 31 * result + (treatmentArms != null ? treatmentArms.hashCode() : 0);
    return result;
  }
}
