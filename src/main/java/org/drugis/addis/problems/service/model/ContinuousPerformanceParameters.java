package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousPerformanceParameters extends PerformanceParameters {
  Double mu;
  Double sigma;

  public ContinuousPerformanceParameters(Double mu, Double sigma) {
    this.mu = mu;
    this.sigma = sigma;
  }

  public Double getMu() {
    return mu;
  }

  public Double getSigma() {
    return sigma;
  }
}
