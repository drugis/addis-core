package org.drugis.addis.problems.model;

/**
 * Created by connor on 8-3-16.
 */
public class Baseline {
  private String scale;
  private Double mu;
  private Double sigma;
  private String name;
  private String type = "dnorm";

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
