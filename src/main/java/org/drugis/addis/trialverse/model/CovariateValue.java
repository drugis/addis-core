package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 12/3/15.
 */
public class CovariateValue {
  private String covariateKey;
  private Double value;

  public CovariateValue(String covariateKey, Double value) {
    this.covariateKey = covariateKey;
    this.value = value;
  }

  public Double getValue() {
    return value;
  }

  public String getCovariateKey() {
    return covariateKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CovariateValue that = (CovariateValue) o;

    if (!covariateKey.equals(that.covariateKey)) return false;
    return !(value != null ? !value.equals(that.value) : that.value != null);

  }

  @Override
  public int hashCode() {
    int result = covariateKey.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
