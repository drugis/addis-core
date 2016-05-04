package org.drugis.addis.trialverse.controller;

import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;

/**
 * Created by connor on 12/1/15.
 */
public class CovariateViewAdapter {

  private String key;
  private String label;
  private String typeKey;
  private String typeLabel;

  public CovariateViewAdapter() {
  }

  public CovariateViewAdapter(CovariateOption option) {
    this.key = option.toString();
    this.label = option.getLabel();
    this.typeKey = option.getType().toString();
    this.typeLabel = option.getType().getLabel();
  }

  public CovariateViewAdapter(SemanticVariable populationCharacteristic) {
    this.key = populationCharacteristic.getUri().toString();
    this.label = populationCharacteristic.getLabel();
    this.typeKey = CovariateOptionType.POPULATION_CHARACTERISTIC.toString();
    this.typeLabel = CovariateOptionType.POPULATION_CHARACTERISTIC.getLabel();
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

    CovariateViewAdapter that = (CovariateViewAdapter) o;

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
