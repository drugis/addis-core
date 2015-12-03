package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 12/3/15.
 */
public class CovariateNumberValue extends CovariateValue {

  private Double value;

  public CovariateNumberValue(String covariateKey, Double value) {
    super(covariateKey);
    this.value = value;
  }

  @Override
  public Double getValue() {
    return value;
  }
}
