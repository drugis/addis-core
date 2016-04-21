package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

public class Measurement {

  private URI studyUid;
  private URI variableUri;
  private URI variableConceptUri;
  private URI armUri;
  private Integer sampleSize;
  private Integer rate;
  private Double stdDev;
  private Double mean;

  public Measurement() {
  }

  public Measurement(URI studyUid, URI variableUri, URI variableConceptUri, URI armUri, Integer sampleSize, Integer rate, Double stdDev, Double mean) {
    this.studyUid = studyUid;
    this.variableUri = variableUri;
    this.variableConceptUri = variableConceptUri;
    this.armUri = armUri;
    this.sampleSize = sampleSize;
    this.rate = rate;
    this.stdDev = stdDev;
    this.mean = mean;
  }

  public URI getStudyUid() {
    return studyUid;
  }

  public URI getVariableUri() {
    return variableUri;
  }

  public URI getVariableConceptUri() {
    return variableConceptUri;
  }

  public URI getArmUri() {
    return armUri;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public Integer getRate() {
    return rate;
  }

  public Double getStdDev() {
    return stdDev;
  }

  public Double getMean() {
    return mean;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (!studyUid.equals(that.studyUid)) return false;
    if (!variableUri.equals(that.variableUri)) return false;
    if (!variableConceptUri.equals(that.variableConceptUri)) return false;
    if (!armUri.equals(that.armUri)) return false;
    if (!sampleSize.equals(that.sampleSize)) return false;
    if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    return mean != null ? mean.equals(that.mean) : that.mean == null;

  }

  @Override
  public int hashCode() {
    int result = studyUid.hashCode();
    result = 31 * result + variableUri.hashCode();
    result = 31 * result + variableConceptUri.hashCode();
    result = 31 * result + armUri.hashCode();
    result = 31 * result + sampleSize.hashCode();
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    return result;
  }
}
