package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousMeasurementEntry extends AbstractMeasurementEntry {
  private ContinuousPerformance performance;

  public ContinuousMeasurementEntry(String alternativeUri, String criterionUri, ContinuousPerformance performance) {
    super(alternativeUri, criterionUri);
    this.performance = performance;
  }

  @Override
  public ContinuousPerformance getPerformance() {
    return this.performance;
  }
}
