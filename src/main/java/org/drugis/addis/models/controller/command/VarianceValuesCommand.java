package org.drugis.addis.models.controller.command;

/**
 * Created by daan on 22-10-15.
 */
public class VarianceValuesCommand {
  private Double mean;
  private Double stdDev;

  public VarianceValuesCommand() {
  }

  public VarianceValuesCommand(Double mean, Double stdDev) {
    this.mean = mean;
    this.stdDev = stdDev;
  }

  public Double getMean() {
    return mean;
  }

  public Double getStdDev() {
    return stdDev;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VarianceValuesCommand that = (VarianceValuesCommand) o;

    if (!mean.equals(that.mean)) return false;
    return stdDev.equals(that.stdDev);

  }

  @Override
  public int hashCode() {
    int result = mean.hashCode();
    result = 31 * result + stdDev.hashCode();
    return result;
  }
}
