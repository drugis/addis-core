package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataIntervention {
  private Long drugId;
  private String uri;

  private Long studyId;

  public TrialDataIntervention(String drugUid, String uri, String studyUid) {
    this.drugId = drugUid;
    this.uri = uri;
    this.studyId = studyUid;
  }

  public Long getDrugId() {
    return drugId;
  }

  public String getUri() {
    return uri;
  }

  public Long getStudyId() {
    return studyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataIntervention)) return false;

    TrialDataIntervention that = (TrialDataIntervention) o;

    if (!drugId.equals(that.drugId)) return false;
    if (!studyId.equals(that.studyId)) return false;
    if (!uri.equals(that.uri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = drugId.hashCode();
    result = 31 * result + uri.hashCode();
    result = 31 * result + studyId.hashCode();
    return result;
  }
}
