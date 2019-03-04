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

  private Double confidenceIntervalWidth;
  private Double confidenceIntervalUpperBound;
  private Double confidenceIntervalLowerBound;
  private Double meanDifference;
  private Double standardizedMeanDifference;
  private Double oddsRatio;
  private Double riskRatio;
  private Double hazardRatio;
  private Boolean isLog;
  private URI referenceArm;
  private Double referenceStdErr;


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

  public MeasurementBuilder setCount(Integer rate) {
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

  public MeasurementBuilder setConfidenceIntervalWidth(Double confidenceIntervalWidth) {
    this.confidenceIntervalWidth = confidenceIntervalWidth;
    return this;
  }

  public MeasurementBuilder setConfidenceIntervalUpperBound(Double confidenceIntervalUpperBound) {
    this.confidenceIntervalUpperBound = confidenceIntervalUpperBound;
    return this;
  }

  public MeasurementBuilder setConfidenceIntervalLowerBound(Double confidenceIntervalLowerBound) {
    this.confidenceIntervalLowerBound = confidenceIntervalLowerBound;
    return this;
  }

  public MeasurementBuilder setMeanDifference(Double meanDifference) {
    this.meanDifference = meanDifference;
    return this;
  }

  public MeasurementBuilder setStandardizedMeanDifference(Double standardizedMeanDifference) {
    this.standardizedMeanDifference = standardizedMeanDifference;
    return this;
  }

  public MeasurementBuilder setOddsRatio(Double oddsRatio) {
    this.oddsRatio = oddsRatio;
    return this;
  }

  public MeasurementBuilder setRiskRatio(Double riskRatio) {
    this.riskRatio = riskRatio;
    return this;
  }

  public MeasurementBuilder setHazardRatio(Double hazardRatio) {
    this.hazardRatio = hazardRatio;
    return this;
  }

  public MeasurementBuilder setLog(Boolean log) {
    isLog = log;
    return this;
  }

  public MeasurementBuilder setReferenceArm(URI referenceArm) {
    this.referenceArm = referenceArm;
    return this;
  }

  public MeasurementBuilder setReferenceStdErr(Double referenceStdErr) {
    this.referenceStdErr = referenceStdErr;
    return this;
  }

  public Measurement build() {
    return new Measurement(studyUuid, variableUri, variableConceptUri, armUri, measurementTypeURI,
            survivalTimeScale, sampleSize, rate, stdDev, stdErr, mean, exposure, confidenceIntervalWidth,
            confidenceIntervalUpperBound, confidenceIntervalLowerBound, meanDifference,
            standardizedMeanDifference, oddsRatio, riskRatio, hazardRatio, isLog, referenceArm, referenceStdErr);
  }
}