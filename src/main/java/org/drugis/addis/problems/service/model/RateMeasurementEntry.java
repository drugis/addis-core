package org.drugis.addis.problems.service.model;

import java.util.Objects;

/**
 * Created by daan on 3/26/14.
 */
public class RateMeasurementEntry extends AbstractMeasurementEntry {

  private RatePerformance performance;
  private Integer alternative;

  public RateMeasurementEntry(Integer alternative, String criterion, String dataSource, RatePerformance performance) {
    super(criterion, dataSource);
    this.alternative = alternative;
    this.performance = performance;
  }

  @Override
  public RatePerformance getPerformance() {
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
    RateMeasurementEntry that = (RateMeasurementEntry) o;
    return Objects.equals(performance, that.performance) &&
            Objects.equals(alternative, that.alternative);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), performance, alternative);
  }
}
