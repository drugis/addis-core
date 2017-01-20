package org.drugis.addis.statistics.command;

/**
 * Created by daan on 20-1-17.
 */
public class DichotomousMeasurementCommandEntry extends MeasurementCommandEntry {
  private Integer count;
  private Integer sampleSize;

  public DichotomousMeasurementCommandEntry() {
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

    DichotomousMeasurementCommandEntry that = (DichotomousMeasurementCommandEntry) o;

    if (count != null ? !count.equals(that.count) : that.count != null) return false;
    return sampleSize != null ? sampleSize.equals(that.sampleSize) : that.sampleSize == null;
  }

  @Override
  public int hashCode() {
    int result = count != null ? count.hashCode() : 0;
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    return result;
  }
}
