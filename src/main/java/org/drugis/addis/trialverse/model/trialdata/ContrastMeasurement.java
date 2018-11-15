package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

public class ContrastMeasurement extends AbstractMeasurement {
  private double stdErr;
  private double confidenceIntervalWidth;
  private double confidenceIntervalLowerBound;
  private double confidenceIntervalUpperBound;
  private double meanDifference;
  private double standardizedMeanDifference;
  private double oddsRatio;
  private double riskRatio;
  private double hazardRatio;
  private boolean isLog;
  private URI referenceArm;
  private double referenceStdErr;
  
}
