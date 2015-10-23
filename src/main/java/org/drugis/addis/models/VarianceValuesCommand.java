package org.drugis.addis.models;

/**
 * Created by daan on 22-10-15.
 */
public class VarianceValuesCommand extends HeterogeneityValuesCommand {
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
}
