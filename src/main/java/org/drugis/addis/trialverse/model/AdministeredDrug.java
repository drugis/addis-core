package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 19-8-14.
 */
public abstract class AdministeredDrug {

  private String drugUid;
  private String drugLabel;

  protected AdministeredDrug(String drugUid, String drugLabel) {
    this.drugUid = drugUid;
    this.drugLabel = drugLabel;
  }

  public String getDrugUid() {
    return drugUid;
  }

  public String getDrugLabel() {
    return drugLabel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AdministeredDrug)) return false;

    AdministeredDrug that = (AdministeredDrug) o;

    if (!drugLabel.equals(that.drugLabel)) return false;
    if (!drugUid.equals(that.drugUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = drugUid.hashCode();
    result = 31 * result + drugLabel.hashCode();
    return result;
  }
}