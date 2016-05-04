package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public class RatePerformanceParameters extends PerformanceParameters {
  private Integer alpha;
  private Integer beta;

  public RatePerformanceParameters(Integer alpha, Integer beta) {
    this.alpha = alpha;
    this.beta = beta;
  }

  public Integer getAlpha() {
    return this.alpha;
  }

  public Integer getBeta() {
    return this.beta;
  }

}
