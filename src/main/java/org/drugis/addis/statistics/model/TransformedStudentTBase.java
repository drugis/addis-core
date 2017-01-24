package org.drugis.addis.statistics.model;

import org.apache.commons.math3.distribution.TDistribution;

/**
 * Created by joris on 24-1-17.
 */
public abstract class TransformedStudentTBase implements Distribution {
  protected final Double mu;
  protected final Double sigma;
  protected final TDistribution distribution;

  TransformedStudentTBase(Double mu, Double sigma, Integer degreesOfFreedom) {
    if (Double.isNaN(mu)) throw new IllegalArgumentException("mu may not be NaN");
    if (Double.isNaN(sigma)) throw new IllegalArgumentException("sigma may not be NaN");
    if (sigma < 0.0) throw new IllegalArgumentException("sigma must be >= 0.0");
    if (degreesOfFreedom < 1) throw new IllegalArgumentException("degreesOfFreedom must be >= 1");
    this.mu = mu;
    this.sigma = sigma;
    distribution = new TDistribution(degreesOfFreedom);
  }

  Double calculateQuantile(Double p) {
    return distribution.inverseCumulativeProbability(p) * sigma + mu;
  }

  Double calculateCumulativeProbability(Double x) {
    return distribution.cumulativeProbability((x - mu) / sigma);
  }

  public Double getMu() {
    return mu;
  }

  public Double getSigma() {
    return sigma;
  }

}
