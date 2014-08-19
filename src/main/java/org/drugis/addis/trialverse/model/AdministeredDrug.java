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
}