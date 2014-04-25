package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private String alternative;
  private String criterion;

  protected AbstractMeasurementEntry(String alternative, String criterion) {
    this.alternative = alternative;
    this.criterion = criterion;
  }

  public String getAlternative() {
    return this.alternative;
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
