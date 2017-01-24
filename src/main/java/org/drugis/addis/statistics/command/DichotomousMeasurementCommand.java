package org.drugis.addis.statistics.command;

import java.net.URI;

/**
 * Created by daan on 20-1-17.
 */
public class DichotomousMeasurementCommand extends AbstractMeasurementCommand {
  private Integer count;
  private Integer sampleSize;

  public DichotomousMeasurementCommand() {
  }

  public DichotomousMeasurementCommand(URI endpointUri, URI armUri, Integer count, Integer sampleSize) {
    super(endpointUri, armUri);
    this.count = count;
    this.sampleSize = sampleSize;
  }

  public Integer getCount() {
    return count;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DichotomousMeasurementCommand that = (DichotomousMeasurementCommand) o;

    if (count != null ? !count.equals(that.count) : that.count != null) return false;
    return sampleSize != null ? sampleSize.equals(that.sampleSize) : that.sampleSize == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (count != null ? count.hashCode() : 0);
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    return result;
  }
}
