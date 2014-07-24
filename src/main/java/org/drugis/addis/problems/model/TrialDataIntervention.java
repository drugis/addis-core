package org.drugis.addis.problems.model;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataIntervention {
  private String drugInstanceUid;
  private String drugConceptUid;

  private String studyUid;

  public TrialDataIntervention() {
  }

  public TrialDataIntervention(String drugInstanceUid, String drugConceptUid, String studyUid) {
    this.drugInstanceUid = drugInstanceUid;
    this.drugConceptUid = drugConceptUid;
    this.studyUid = studyUid;
  }

  public String getDrugInstanceUid() {
    return drugInstanceUid;
  }

  public String getDrugConceptUid() {
    return drugConceptUid;
  }

  public String getStudyUid() {
    return studyUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataIntervention)) return false;

    TrialDataIntervention that = (TrialDataIntervention) o;

    if (!drugInstanceUid.equals(that.drugInstanceUid)) return false;
    if (!studyUid.equals(that.studyUid)) return false;
    if (!drugConceptUid.equals(that.drugConceptUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = drugInstanceUid.hashCode();
    result = 31 * result + drugConceptUid.hashCode();
    result = 31 * result + studyUid.hashCode();
    return result;
  }
}
