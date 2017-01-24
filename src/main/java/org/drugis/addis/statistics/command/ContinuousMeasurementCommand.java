package org.drugis.addis.statistics.command;

import java.net.URI;

/**
 * Created by joris on 24-1-17.
 */
public class ContinuousMeasurementCommand extends AbstractMeasurementCommand {
  private Double mean;
  private Double stdDev;
  private Integer sampleSize;

  public ContinuousMeasurementCommand() {
  }

  public ContinuousMeasurementCommand(URI endpointUri, URI armUri, Double mean, Double stdDev, Integer sampleSize) {
    super(endpointUri, armUri);
    this.mean = mean;
    this.stdDev = stdDev;
    this.sampleSize = sampleSize;
  }

  public Double getMean() {
    return mean;
  }

  public Double getStdDev() {
    return stdDev;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousMeasurementCommand that = (ContinuousMeasurementCommand) o;

    if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    return sampleSize != null ? sampleSize.equals(that.sampleSize) : that.sampleSize == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    return result;
  }
}
