package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public abstract class AbstractMeasurementEntry {
  private String alternativeUri;
  private String criterionUri;

  protected AbstractMeasurementEntry(String alternativeUri, String criterionUri) {
    this.alternativeUri = alternativeUri;
    this.criterionUri = criterionUri;
  }

  public String getAlternativeUri() {
    return this.alternativeUri;
  }

  public String getCriterionUri() {
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
