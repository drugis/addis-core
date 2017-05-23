package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private Integer alternative;
  private URI criterion; // nb not necessarily an uri in other cases of br-problem

  protected AbstractMeasurementEntry(Integer alternative, URI criterion) {
    this.alternative = alternative;
    this.criterion = criterion;
  }

  public String getAlternative() {
    return alternative.toString();
  }

  public URI getCriterion() {
    return this.criterion;
  }

  public abstract AbstractPerformance getPerformance();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractMeasurementEntry that = (AbstractMeasurementEntry) o;

    if (!alternative.equals(that.alternative)) return false;
    if (!criterion.equals(that.criterion)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = alternative.hashCode();
    result = 31 * result + criterion.hashCode();
    return result;
  }
}
