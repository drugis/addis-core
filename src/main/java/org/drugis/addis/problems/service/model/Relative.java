package org.drugis.addis.problems.service.model;

import java.util.Map;

/**
 * Created by joris on 14-6-17.
 */
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

    if (!type.equals(relative.type)) return false;
    if (!mu.equals(relative.mu)) return false;
    return cov.equals(relative.cov);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + mu.hashCode();
    result = 31 * result + cov.hashCode();
    return result;
  }
}
