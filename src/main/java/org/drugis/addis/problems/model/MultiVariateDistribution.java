package org.drugis.addis.problems.model;

import java.util.HashMap;
import java.util.Map;

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

      if (mu != null ? !mu.equals(that.mu) : that.mu != null) return false;
      return sigma != null ? sigma.equals(that.sigma) : that.sigma == null;

    }

    @Override
    public int hashCode() {
      int result = mu != null ? mu.hashCode() : 0;
      result = 31 * result + (sigma != null ? sigma.hashCode() : 0);
      return result;
    }
  }

