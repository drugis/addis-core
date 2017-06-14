package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/26/14.
 */
public class RateMeasurementEntry extends AbstractMeasurementEntry {
  private RatePerformance performance;
  private Integer alternative;

  public RateMeasurementEntry(Integer alternative, String criterion, RatePerformance performance) {
    super(criterion);
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
