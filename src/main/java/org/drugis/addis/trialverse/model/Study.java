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
  private String name;
  private String title;
  private List<String> outcomeUids = new ArrayList<>();
  private Set<StudyTreatmentArm> treatmentArms = new HashSet<>();

  public Study() {
  }

  public Study(String uid, String name, String title, List<String> outcomeUids) {
    this.uid = uid;
    this.name = name;
    this.title = title;
    this.outcomeUids = outcomeUids;
  }

  public String getUid() {
    return uid;
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

    if (!name.equals(study.name)) return false;
    if (outcomeUids != null ? !outcomeUids.equals(study.outcomeUids) : study.outcomeUids != null) return false;
    if (!title.equals(study.title)) return false;
    if (treatmentArms != null ? !treatmentArms.equals(study.treatmentArms) : study.treatmentArms != null) return false;
    if (!uid.equals(study.uid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (outcomeUids != null ? outcomeUids.hashCode() : 0);
    result = 31 * result + (treatmentArms != null ? treatmentArms.hashCode() : 0);
    return result;
  }
}
