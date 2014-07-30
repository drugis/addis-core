package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private String uid;
  private String name;
  private String studyUid;
  private String drugInstanceUid;
  private String drugConceptUid;
  private Measurement measurement;

  public TrialDataArm() {
  }

  public TrialDataArm(String uid, String name, String studyUid, String drugInstanceUid, String drugConceptUid, Measurement measurement) {
    this.uid = uid;
    this.name = name;
    this.studyUid = studyUid;
    this.drugInstanceUid = drugInstanceUid;
    this.drugConceptUid = drugConceptUid;
    this.measurement = measurement;
  }

  public String getUid() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public String getStudyUid() {
    return studyUid;
  }

  public String getDrugInstanceUid() {
    return drugInstanceUid;
  }

  public String getDrugConceptUid() {
    return drugConceptUid;
  }

  public Measurement getMeasurement() {
    return measurement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataArm)) return false;

    TrialDataArm that = (TrialDataArm) o;

    if (!drugInstanceUid.equals(that.drugInstanceUid)) return false;
    if (!drugConceptUid.equals(that.drugConceptUid)) return false;
    if (!measurement.equals(that.measurement)) return false;
    if (!name.equals(that.name)) return false;
    if (!studyUid.equals(that.studyUid)) return false;
    if (!uid.equals(that.uid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + studyUid.hashCode();
    result = 31 * result + drugInstanceUid.hashCode();
    result = 31 * result + drugConceptUid.hashCode();
    result = 31 * result + measurement.hashCode();
    return result;
  }
}
