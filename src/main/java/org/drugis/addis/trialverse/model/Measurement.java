package org.drugis.addis.trialverse.model;

import java.net.URI;

public class Measurement {

  private URI studyUid;
  private URI variableUri;
  private URI armUri;
  private Integer sampleSize;
  private Integer rate;
  private Double stdDev;
  private Double mean;

  public Measurement() {
  }

  public Measurement(URI studyUid, URI variableUri, URI armUri, Integer sampleSize, Integer rate, Double stdDev, Double mean) {
    this.studyUid = studyUid;
    this.variableUri = variableUri;
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
    if (!(o instanceof Measurement)) return false;

    Measurement that = (Measurement) o;

    if (!armUri.equals(that.armUri)) return false;
    if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
    if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
    if (!sampleSize.equals(that.sampleSize)) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    if (!studyUid.equals(that.studyUid)) return false;
    if (!variableUri.equals(that.variableUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyUid.hashCode();
    result = 31 * result + variableUri.hashCode();
    result = 31 * result + armUri.hashCode();
    result = 31 * result + sampleSize.hashCode();
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    return result;
  }
}
