package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private Integer alternative;
  private URI criterionUri;

  protected AbstractMeasurementEntry(Integer alternative, URI criterionUri) {
    this.alternative = alternative;
    this.criterionUri = criterionUri;
  }

  public String getAlternative() {
    return alternative.toString();
  }

  public URI getCriterionUri() {
    return this.criterionUri;
  }

  public abstract AbstractPerformance getPerformance();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractMeasurementEntry that = (AbstractMeasurementEntry) o;

    if (!alternative.equals(that.alternative)) return false;
    if (!criterionUri.equals(that.criterionUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = alternative.hashCode();
    result = 31 * result + criterionUri.hashCode();
    return result;
  }
}
