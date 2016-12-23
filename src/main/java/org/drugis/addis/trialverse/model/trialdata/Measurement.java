package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

public class Measurement {

  private URI studyUuid;
  private URI variableUri;
  private URI variableConceptUri;
  private URI armUri;
  private URI measurementTypeURI;
  private Integer sampleSize;
  private Integer rate;
  private Double stdDev;
  private Double stdErr;
  private Double mean;

  public Measurement() {
  }

  public Measurement(URI studyUuid, URI variableUri, URI variableConceptUri, URI armUri, URI measurementTypeURI,Integer sampleSize, Integer rate, Double stdDev, Double stdErr, Double mean) {
    this.studyUuid = studyUuid;
    this.variableUri = variableUri;
    this.variableConceptUri = variableConceptUri;
    this.armUri = armUri;
    this.measurementTypeURI = measurementTypeURI;
    this.sampleSize = sampleSize;
    this.rate = rate;
    this.stdDev = stdDev;
    this.stdErr = stdErr;
    this.mean = mean;
  }

  public URI getStudyUid() {
    return studyUuid;
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

  public URI getMeasurementTypeURI() {
    return measurementTypeURI;
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

  public Double getStdErr() {
    return stdErr;
  }

  public Double getMean() {
    return mean;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (!studyUuid.equals(that.studyUuid)) return false;
    if (!variableUri.equals(that.variableUri)) return false;
    if (variableConceptUri != null ? !variableConceptUri.equals(that.variableConceptUri) : that.variableConceptUri != null)
      return false;
    if (!armUri.equals(that.armUri)) return false;
    if (!measurementTypeURI.equals(that.measurementTypeURI)) return false;
    if (sampleSize != null ? !sampleSize.equals(that.sampleSize) : that.sampleSize != null) return false;
    if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    if (stdErr != null ? !stdErr.equals(that.stdErr) : that.stdErr != null) return false;
    return mean != null ? mean.equals(that.mean) : that.mean == null;

  }

  @Override
  public int hashCode() {
    int result = studyUuid.hashCode();
    result = 31 * result + variableUri.hashCode();
    result = 31 * result + (variableConceptUri != null ? variableConceptUri.hashCode() : 0);
    result = 31 * result + armUri.hashCode();
    result = 31 * result + measurementTypeURI.hashCode();
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (stdErr != null ? stdErr.hashCode() : 0);
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    return result;
  }
}
