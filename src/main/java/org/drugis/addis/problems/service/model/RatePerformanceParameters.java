package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public class RatePerformanceParameters extends PerformanceParameters {
  private Long alpha;
  private Long beta;

  public RatePerformanceParameters(Long alpha, Long beta) {
    this.alpha = alpha;
    this.beta = beta;
  }

  public Long getAlpha() {
    return this.alpha;
  }

  public Long getBeta() {
    return this.beta;
  }

}
