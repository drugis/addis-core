package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private String criterion; // nb not necessarily an uri in other cases of br-problem

  protected AbstractMeasurementEntry(String criterion) {
    this.criterion = criterion;
  }

  public String getCriterion() {
    return this.criterion;
  }

  public abstract AbstractPerformance getPerformance();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractMeasurementEntry that = (AbstractMeasurementEntry) o;

    return criterion.equals(that.criterion);
  }

  @Override
  public int hashCode() {
    return criterion.hashCode();
  }
}
