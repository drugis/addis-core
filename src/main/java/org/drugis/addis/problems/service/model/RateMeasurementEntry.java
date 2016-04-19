package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/26/14.
 */
public class RateMeasurementEntry extends AbstractMeasurementEntry {
  private RatePerformance performance;

  public RateMeasurementEntry(URI alternativeUri, URI criterionUri, RatePerformance performance) {
    super(alternativeUri, criterionUri);
    this.performance = performance;
  }

  @Override
  public RatePerformance getPerformance() {
    return this.performance;
  }
}
