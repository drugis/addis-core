package org.drugis.addis.problems.service.model;

import java.util.Objects;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousMeasurementEntry extends AbstractMeasurementEntry {
  private ContinuousPerformance performance;
  private Integer alternative;

  public ContinuousMeasurementEntry(Integer interventionId, String criterion, String dataSource, ContinuousPerformance performance) {
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
    return Objects.equals(performance, that.performance) &&
        Objects.equals(alternative, that.alternative);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), performance, alternative);
  }
}
