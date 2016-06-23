package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousMeasurementEntry extends AbstractMeasurementEntry {
  private ContinuousPerformance performance;

  public ContinuousMeasurementEntry(Integer interventionId, URI criterionUri, ContinuousPerformance performance) {
    super(interventionId, criterionUri);
    this.performance = performance;
  }

  @Override
  public ContinuousPerformance getPerformance() {
    return this.performance;
  }
}
