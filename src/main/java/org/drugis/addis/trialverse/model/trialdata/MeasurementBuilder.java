package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

public class MeasurementBuilder {
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
  private Double exposure;
  private String survivalTimeScale;

  public MeasurementBuilder(URI studyUri, URI variableUri, URI variableConceptUri, URI armUri, URI measurementTypeURI) {
    this.studyUuid = studyUri;
    this.variableUri = variableUri;
    this.variableConceptUri = variableConceptUri;
    this.armUri = armUri;
    this.measurementTypeURI = measurementTypeURI;
  }

  public MeasurementBuilder setStudyUuid(URI studyUuid) {
    this.studyUuid = studyUuid;
    return this;
  }

  public MeasurementBuilder setVariableUri(URI variableUri) {
    this.variableUri = variableUri;
    return this;
  }

  public MeasurementBuilder setVariableConceptUri(URI variableConceptUri) {
    this.variableConceptUri = variableConceptUri;
    return this;
  }

  public MeasurementBuilder setSurvivalTimeScale(String survivalTimeScale) {
    this.survivalTimeScale = survivalTimeScale;
    return this;
  }

  public MeasurementBuilder setArmUri(URI armUri) {
    this.armUri = armUri;
    return this;
  }

  public MeasurementBuilder setMeasurementTypeURI(URI measurementTypeURI) {
    this.measurementTypeURI = measurementTypeURI;
    return this;
  }

  public MeasurementBuilder setSampleSize(Integer sampleSize) {
    this.sampleSize = sampleSize;
    return this;
  }

  public MeasurementBuilder setRate(Integer rate) {
    this.rate = rate;
    return this;
  }

  public MeasurementBuilder setStdDev(Double stdDev) {
    this.stdDev = stdDev;
    return this;
  }

  public MeasurementBuilder setStdErr(Double stdErr) {
    this.stdErr = stdErr;
    return this;
  }

  public MeasurementBuilder setMean(Double mean) {
    this.mean = mean;
    return this;
  }

  public MeasurementBuilder setExposure(Double exposure) {
    this.exposure = exposure;
    return this;
  }

  public Measurement createMeasurement() {
    return new Measurement(studyUuid, variableUri, variableConceptUri, survivalTimeScale, armUri, measurementTypeURI, sampleSize, rate, stdDev, stdErr, mean, exposure);
  }
}