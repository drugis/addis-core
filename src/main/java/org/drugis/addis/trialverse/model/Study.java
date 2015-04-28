package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 3/19/14.
 */
public class Study {

  private String uid;
  private String studyGraphUuid;
  private String name;
  private String title;
  private List<String> outcomeUids = new ArrayList<>();
  private Set<StudyTreatmentArm> treatmentArms = new HashSet<>();

  public Study() {
  }

  public Study(String uid, String studyGraphUuid, String name, String title, List<String> outcomeUids) {
    this.uid = uid;
    this.studyGraphUuid = studyGraphUuid;
    this.name = name;
    this.title = title;
    this.outcomeUids = outcomeUids;
  }

  public String getUid() {
    return uid;
  }

  public String getStudyGraphUuid() {
    return studyGraphUuid;
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

    if (uid != null ? !uid.equals(study.uid) : study.uid != null) return false;
    if (studyGraphUuid != null ? !studyGraphUuid.equals(study.studyGraphUuid) : study.studyGraphUuid != null)
      return false;
    if (name != null ? !name.equals(study.name) : study.name != null) return false;
    if (title != null ? !title.equals(study.title) : study.title != null) return false;
    if (outcomeUids != null ? !outcomeUids.equals(study.outcomeUids) : study.outcomeUids != null) return false;
    return !(treatmentArms != null ? !treatmentArms.equals(study.treatmentArms) : study.treatmentArms != null);

  }

  @Override
  public int hashCode() {
    int result = uid != null ? uid.hashCode() : 0;
    result = 31 * result + (studyGraphUuid != null ? studyGraphUuid.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (outcomeUids != null ? outcomeUids.hashCode() : 0);
    result = 31 * result + (treatmentArms != null ? treatmentArms.hashCode() : 0);
    return result;
  }
}
