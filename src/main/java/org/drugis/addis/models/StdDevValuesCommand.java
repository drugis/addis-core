package org.drugis.addis.models;

/**
 * Created by daan on 22-10-15.
 */
public class StdDevValuesCommand extends HeterogeneityValuesCommand {
  private final Double lower;
  private final Double upper;

  public StdDevValuesCommand(Double lower, Double upper) {

    this.lower = lower;
    this.upper = upper;
  }

  public Double getLower() {
    return lower;
  }

  public Double getUpper() {
    return upper;
  }
}
