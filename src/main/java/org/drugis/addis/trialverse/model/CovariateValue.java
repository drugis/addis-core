package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 12/3/15.
 */
public abstract class CovariateValue {
  private String covariateKey;

  public CovariateValue(String covariateKey) {
    this.covariateKey = covariateKey;
  }

  public abstract Object getValue();

  public String getCovariateKey() {
    return covariateKey;
  }


}
