package org.drugis.addis.problems.model.problemEntry;

import java.util.Objects;

/**
 * Created by daan on 21-5-14.
 */
public abstract class AbstractProblemEntry {
  private String study;
  private Integer treatment;

  public AbstractProblemEntry(String study, Integer treatment) {
    this.study = study;
    this.treatment = treatment;
  }

  public String getStudy() {
    return study;
  }

  public Integer getTreatment() {
    return treatment;
  }

  public abstract boolean hasMissingValues();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractProblemEntry that = (AbstractProblemEntry) o;
    return Objects.equals(study, that.study) &&
            Objects.equals(treatment, that.treatment);
  }

  @Override
  public int hashCode() {

    return Objects.hash(study, treatment);
  }
}
