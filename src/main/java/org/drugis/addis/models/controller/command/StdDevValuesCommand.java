package org.drugis.addis.models.controller.command;

/**
 * Created by daan on 22-10-15.
 */
public class StdDevValuesCommand {
  private Double lower;
  private Double upper;

  public StdDevValuesCommand() {
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StdDevValuesCommand that = (StdDevValuesCommand) o;

    if (!lower.equals(that.lower)) return false;
    return upper.equals(that.upper);

  }

  @Override
  public int hashCode() {
    int result = lower.hashCode();
    result = 31 * result + upper.hashCode();
    return result;
  }
}
