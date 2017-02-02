package org.drugis.addis.statistics.model;

import java.net.URI;

/**
 * Created by joris on 24-1-17.
 */
public class Estimate {
  private Double confidenceIntervalLowerBound;
  private Double confidenceIntervalUpperBound;
  private Double pValue;
  private Double pointEstimate;
  private URI armUri;

  public Estimate() {
  }

  public Estimate(Double pointEstimate, Double confidenceIntervalLowerBound, Double confidenceIntervalUpperBound, Double pValue, URI armUri) {
    this.pointEstimate = pointEstimate;
    this.confidenceIntervalLowerBound = confidenceIntervalLowerBound;
    this.confidenceIntervalUpperBound = confidenceIntervalUpperBound;
    this.pValue = pValue;
    this.armUri = armUri;
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

  public URI getArmUri() {
    return armUri;
  }
}
