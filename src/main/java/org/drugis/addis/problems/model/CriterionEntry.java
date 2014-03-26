package org.drugis.addis.problems.model;

import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String title;
  private List<Double> scale;
  private PartialValueFunction partialValueFunction;

  public CriterionEntry(String title, List<Double> scale, PartialValueFunction partialValueFunction) {
    this.title = title;
    this.scale = scale;
    this.partialValueFunction = partialValueFunction;
  }

  public String getTitle() {
    return title;
  }

  public List<Double> getScale() {
    return scale;
  }

  public PartialValueFunction getPartialValueFunction() {
    return partialValueFunction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CriterionEntry)) return false;

    CriterionEntry that = (CriterionEntry) o;

    if (partialValueFunction != null ? !partialValueFunction.equals(that.partialValueFunction) : that.partialValueFunction != null)
      return false;
    if (scale != null ? !scale.equals(that.scale) : that.scale != null) return false;
    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + (scale != null ? scale.hashCode() : 0);
    result = 31 * result + (partialValueFunction != null ? partialValueFunction.hashCode() : 0);
    return result;
  }
}
