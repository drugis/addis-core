package org.drugis.addis.models;

/**
 * Created by daan on 22-10-15.
 */
public class PrecisionValuesCommand extends HeterogeneityValuesCommand {
  private final Double rate;
  private final Double shape;

  public PrecisionValuesCommand(Double rate, Double shape) {

    this.rate = rate;
    this.shape = shape;
  }

  public Double getRate() {
    return rate;
  }

  public Double getShape() {
    return shape;
  }
}
