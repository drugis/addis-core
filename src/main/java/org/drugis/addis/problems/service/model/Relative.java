package org.drugis.addis.problems.service.model;

import java.util.Map;
import java.util.Objects;

public class Relative {
  private String type;
  private Map<String, Double> mu;
  private CovarianceMatrix cov;

  public Relative(String type, Map<String, Double> mu, CovarianceMatrix cov) {
    this.type = type;
    this.mu = mu;
    this.cov = cov;
  }

  public String getType() {
    return type;
  }

  public Map<String, Double> getMu() {
    return mu;
  }

  public CovarianceMatrix getCov() {
    return cov;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Relative relative = (Relative) o;
    return Objects.equals(type, relative.type) &&
        Objects.equals(mu, relative.mu) &&
        Objects.equals(cov, relative.cov);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, mu, cov);
  }
}
