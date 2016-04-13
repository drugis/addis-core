package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 12/3/15.
 */
public class CovariateStudyValue {

  private URI studyUri;
  private String covariateKey;
  private Double value;

  public CovariateStudyValue() {
  }

  public CovariateStudyValue(URI studyUri, String covariateKey, Double value) {
    this.studyUri = studyUri;
    this.covariateKey = covariateKey;
    this.value = value;
  }

  public URI getStudyUri() {
    return studyUri;
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

    CovariateStudyValue that = (CovariateStudyValue) o;

    if (!studyUri.equals(that.studyUri)) return false;
    if (!covariateKey.equals(that.covariateKey)) return false;
    return !(value != null ? !value.equals(that.value) : that.value != null);

  }

  @Override
  public int hashCode() {
    int result = studyUri.hashCode();
    result = 31 * result + covariateKey.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
