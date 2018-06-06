package org.drugis.addis.problems.service.model;

import java.net.URI;
import java.util.Objects;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private String criterion; // nb not necessarily an uri in other cases of br-problem
  private URI dataSource;

  protected AbstractMeasurementEntry(String criterion, URI dataSource) {
    this.criterion = criterion;
    this.dataSource = dataSource;
  }

  public String getCriterion() {
    return this.criterion;
  }

  public URI getDataSource() {
    return dataSource;
  }

  public abstract AbstractPerformance getPerformance();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractMeasurementEntry that = (AbstractMeasurementEntry) o;
    return Objects.equals(criterion, that.criterion) &&
        Objects.equals(dataSource, that.dataSource);
  }

  @Override
  public int hashCode() {

    return Objects.hash(criterion, dataSource);
  }
}
