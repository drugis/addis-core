package org.drugis.addis.problems.service.model;

import java.net.URI;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private URI alternativeUri;
  private URI criterionUri;

  protected AbstractMeasurementEntry(URI alternativeUri, URI criterionUri) {
    this.alternativeUri = alternativeUri;
    this.criterionUri = criterionUri;
  }

  public URI getAlternativeUri() {
    return this.alternativeUri;
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

    if (!alternativeUri.equals(that.alternativeUri)) return false;
    if (!criterionUri.equals(that.criterionUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = alternativeUri.hashCode();
    result = 31 * result + criterionUri.hashCode();
    return result;
  }
}
