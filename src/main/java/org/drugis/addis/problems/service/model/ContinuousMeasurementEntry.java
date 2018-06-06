package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousMeasurementEntry extends AbstractMeasurementEntry {
  private ContinuousPerformance performance;
  private Integer alternative;

  public ContinuousMeasurementEntry(Integer interventionId, String criterion, ContinuousPerformance performance) {
    super(criterion, dataSource);
    this.alternative = interventionId;
    this.performance = performance;
  }

  @Override
  public ContinuousPerformance getPerformance() {
    return this.performance;
  }

  public String getAlternative() {
    return alternative.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousMeasurementEntry that = (ContinuousMeasurementEntry) o;

    if (!performance.equals(that.performance)) return false;
    return alternative.equals(that.alternative);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + performance.hashCode();
    result = 31 * result + alternative.hashCode();
    return result;
  }
}
