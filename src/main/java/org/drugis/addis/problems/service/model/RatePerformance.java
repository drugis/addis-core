package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public class RatePerformance extends AbstractPerformance {
  private RatePerformanceParameters parameters;

  public RatePerformance(RatePerformanceParameters parameters) {
    this.parameters = parameters;
  }

  @Override
  public String getType() {
    return "dnormal";
  }

  @Override
  public PerformanceParameters getParameters() {
    return this.parameters;
  }
}
