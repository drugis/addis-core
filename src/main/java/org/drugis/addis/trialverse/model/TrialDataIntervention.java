package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataIntervention {
  private Long drugId;
  private String uri;

  public TrialDataIntervention(Long drugId, String uri) {
    this.drugId = drugId;
    this.uri = uri;
  }

  public Long getDrugId() {
    return drugId;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataIntervention)) return false;

    TrialDataIntervention that = (TrialDataIntervention) o;

    if (!drugId.equals(that.drugId)) return false;
    if (!uri.equals(that.uri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = drugId.hashCode();
    result = 31 * result + uri.hashCode();
    return result;
  }
}
