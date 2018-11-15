package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.Objects;

public class AbsoluteMeasurement extends AbstractMeasurement {

  private String survivalTimeScale;
  private Integer sampleSize;
  private Integer rate;
  private Double stdDev;
  private Double stdErr;
  private Double mean;
  private Double exposure;

  public AbsoluteMeasurement() {
  }

  public AbsoluteMeasurement(URI studyUuid, URI variableUri, URI variableConceptUri, URI armUri, URI measurementTypeURI, String survivalTimeScale, Integer sampleSize, Integer rate, Double stdDev, Double stdErr, Double mean, Double exposure) {
    super(studyUuid, variableUri, variableConceptUri, armUri, measurementTypeURI);
    this.survivalTimeScale = survivalTimeScale;
    this.sampleSize = sampleSize;
    this.rate = rate;
    this.stdDev = stdDev;
    this.stdErr = stdErr;
    this.mean = mean;
    this.exposure = exposure;
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
    if (!super.equals(o)) return false;
    AbsoluteMeasurement that = (AbsoluteMeasurement) o;
    return Objects.equals(survivalTimeScale, that.survivalTimeScale) &&
            Objects.equals(sampleSize, that.sampleSize) &&
            Objects.equals(rate, that.rate) &&
            Objects.equals(stdDev, that.stdDev) &&
            Objects.equals(stdErr, that.stdErr) &&
            Objects.equals(mean, that.mean) &&
            Objects.equals(exposure, that.exposure);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), survivalTimeScale, sampleSize, rate, stdDev, stdErr, mean, exposure);
  }
}
