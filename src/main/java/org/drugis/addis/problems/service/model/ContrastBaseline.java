package org.drugis.addis.problems.service.model;

import java.util.Objects;

public class ContrastBaseline {
  private String type;
  private String name;
  private Double mu;
  private Double sigma;

  public ContrastBaseline(String type, String name, Double mu, Double sigma) {
    this.type = type;
    this.name = name;
    this.mu = mu;
    this.sigma = sigma;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Double getMu() {
    return mu;
  }

  public Double getSigma() {
    return sigma;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContrastBaseline that = (ContrastBaseline) o;
    return Objects.equals(type, that.type) &&
            Objects.equals(name, that.name) &&
            Objects.equals(mu, that.mu) &&
            Objects.equals(sigma, that.sigma);
  }

  @Override
  public int hashCode() {

    return Objects.hash(type, name, mu, sigma);
  }
}
