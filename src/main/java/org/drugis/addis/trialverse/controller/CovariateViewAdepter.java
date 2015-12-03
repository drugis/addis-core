package org.drugis.addis.trialverse.controller;

import org.drugis.addis.trialverse.model.emun.CovariateOption;

/**
 * Created by connor on 12/1/15.
 */
public class CovariateViewAdepter {

  private String key;
  private String label;
  private String typeKey;
  private String typeLabel;

  public CovariateViewAdepter() {
  }

  public CovariateViewAdepter(CovariateOption option) {
    this.key = option.toString();
    this.label = option.getLabel();
    this.typeKey = option.getType().toString();
    this.typeLabel = option.getType().getLabel();
  }

  public String getKey() {
    return key;
  }

  public String getLabel() {
    return label;
  }

  public String getTypeKey() {
    return typeKey;
  }

  public String getTypeLabel() {
    return typeLabel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CovariateViewAdepter that = (CovariateViewAdepter) o;

    if (!key.equals(that.key)) return false;
    if (!label.equals(that.label)) return false;
    if (!typeKey.equals(that.typeKey)) return false;
    return typeLabel.equals(that.typeLabel);

  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + label.hashCode();
    result = 31 * result + typeKey.hashCode();
    result = 31 * result + typeLabel.hashCode();
    return result;
  }
}
