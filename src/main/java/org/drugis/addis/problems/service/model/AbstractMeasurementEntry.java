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
}
