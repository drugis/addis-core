package org.drugis.addis.statistics.model;

/**
 * Created by joris on 24-1-17.
 */
public class Estimate {
  private Double confidenceIntervalLowerBound;
  private Double confidenceIntervalUpperBound;
  private Double pValue;
  private Double pointEstimate;

  public Estimate(){
  }

  public Estimate(Double pointEstimate, Double confidenceIntervalLowerBound, Double confidenceIntervalUpperBound, Double pValue) {
    this.pointEstimate = pointEstimate;
    this.confidenceIntervalLowerBound = confidenceIntervalLowerBound;
    this.confidenceIntervalUpperBound = confidenceIntervalUpperBound;
    this.pValue = pValue;
  }

  public Double getConfidenceIntervalLowerBound() {
    return confidenceIntervalLowerBound;
  }

  public Double getConfidenceIntervalUpperBound() {
    return confidenceIntervalUpperBound;
  }

  public Double getpValue() {
    return pValue;
  }

  public Double getPointEstimate() {
    return pointEstimate;
  }
}
