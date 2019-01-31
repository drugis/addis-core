package org.drugis.addis.problems.model.problemEntry;

import java.util.Objects;

public abstract class AbstractContrastDataEntry extends AbstractProblemEntry{
  private final Double standardError;

  public AbstractContrastDataEntry(String study, Integer treatment, Double standardError) {
    super(study, treatment);
    this.standardError = standardError;

  }

  public Double getStandardError() {
    return standardError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    AbstractContrastDataEntry that = (AbstractContrastDataEntry) o;
    return Objects.equals(standardError, that.standardError);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), standardError);
  }
}
