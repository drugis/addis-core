package org.drugis.addis.problems.model;

import javax.persistence.Entity;

/**
 * Created by connor on 8-3-16.
 */
@Entity
public class Baseline {
  private String scale;
  private Double mu;
  private Double sigma;
  private String name;
  private String type = "dnorm";

  public Baseline() {
  }

  public Baseline(String scale, Double mu, Double sigma, String name, String type) {
    this.scale = scale;
    this.mu = mu;
    this.sigma = sigma;
    this.name = name;
    this.type = type;
  }

  public String getScale() {
    return scale;
  }

  public Double getMu() {
    return mu;
  }

  public Double getSigma() {
    return sigma;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }
}
