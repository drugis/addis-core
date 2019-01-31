package org.drugis.addis.trialverse.model;

import java.util.Objects;

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
    if (o == null || getClass() != o.getClass()) return false;
    AdministeredDrug that = (AdministeredDrug) o;
    return Objects.equals(drugUid, that.drugUid) &&
            Objects.equals(drugLabel, that.drugLabel);
  }

  @Override
  public int hashCode() {

    return Objects.hash(drugUid, drugLabel);
  }
}