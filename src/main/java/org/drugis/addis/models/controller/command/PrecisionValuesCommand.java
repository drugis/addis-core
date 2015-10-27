package org.drugis.addis.models.controller.command;

/**
 * Created by daan on 22-10-15.
 */
public class PrecisionValuesCommand {
  private Double rate;
  private Double shape;

  public PrecisionValuesCommand() {
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PrecisionValuesCommand that = (PrecisionValuesCommand) o;

    if (!rate.equals(that.rate)) return false;
    return shape.equals(that.shape);

  }

  @Override
  public int hashCode() {
    int result = rate.hashCode();
    result = 31 * result + shape.hashCode();
    return result;
  }
}
