package org.drugis.addis.problems.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by connor on 8-3-16.
 */
public class MultiVariateDistribution {
    private Map<String, Double> mu = new HashMap<>();
    private Map<String, Map<String, Double>> sigma = new HashMap<>();

    public MultiVariateDistribution() {
    }

    public MultiVariateDistribution(Map<String, Double> mu, Map<String, Map<String, Double>> sigma) {
      this.mu = mu;
      this.sigma = sigma;
    }

    public Map<String, Double> getMu() {
      return mu;
    }

    public Map<String, Map<String, Double>> getSigma() {
      return sigma;
    }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MultiVariateDistribution that = (MultiVariateDistribution) o;
    return Objects.equals(mu, that.mu) &&
            Objects.equals(sigma, that.sigma);
  }

  @Override
  public int hashCode() {

    return Objects.hash(mu, sigma);
  }
}

