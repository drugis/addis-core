package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

public class Measurement {

  private URI studyUuid;
  private URI variableUri;
  private URI variableConceptUri;
  private String survivalTimeScale;
  private URI armUri;
  private URI measurementTypeURI;
  private Integer sampleSize;
  private Integer rate;
  private Double stdDev;
  private Double stdErr;
  private Double mean;
  private Double exposure;

  public Measurement() {
  }

  public Measurement(URI studyUuid, URI variableUri, URI variableConceptUri, String survivalTimeScale, URI armUri, URI measurementTypeURI, Integer sampleSize,
                     Integer rate, Double stdDev, Double stdErr, Double mean, Double exposure) {
    this.studyUuid = studyUuid;
    this.variableUri = variableUri;
    this.variableConceptUri = variableConceptUri;
    this.survivalTimeScale = survivalTimeScale;
    this.armUri = armUri;
    this.measurementTypeURI = measurementTypeURI;
    this.sampleSize = sampleSize;
    this.rate = rate;
    this.stdDev = stdDev;
    this.stdErr = stdErr;
    this.mean = mean;
    this.exposure = exposure;
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

  public String getSurvivalTimeScale() {
    return survivalTimeScale;
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

  public Double getExposure() {
    return exposure;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (!studyUuid.equals(that.studyUuid)) return false;
    if (!variableUri.equals(that.variableUri)) return false;
    if (!variableConceptUri.equals(that.variableConceptUri)) return false;
    if (survivalTimeScale != null ? !survivalTimeScale.equals(that.survivalTimeScale) : that.survivalTimeScale != null)
      return false;
    if (!armUri.equals(that.armUri)) return false;
    if (!measurementTypeURI.equals(that.measurementTypeURI)) return false;
    if (sampleSize != null ? !sampleSize.equals(that.sampleSize) : that.sampleSize != null) return false;
    if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    if (stdErr != null ? !stdErr.equals(that.stdErr) : that.stdErr != null) return false;
    if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
    return exposure != null ? exposure.equals(that.exposure) : that.exposure == null;
  }

  @Override
  public int hashCode() {
    int result = studyUuid.hashCode();
    result = 31 * result + variableUri.hashCode();
    result = 31 * result + variableConceptUri.hashCode();
    result = 31 * result + (survivalTimeScale != null ? survivalTimeScale.hashCode() : 0);
    result = 31 * result + armUri.hashCode();
    result = 31 * result + measurementTypeURI.hashCode();
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (stdErr != null ? stdErr.hashCode() : 0);
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    result = 31 * result + (exposure != null ? exposure.hashCode() : 0);
    return result;
  }
}
