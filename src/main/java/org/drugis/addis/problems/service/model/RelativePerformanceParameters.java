package org.drugis.addis.problems.service.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Created by joris on 14-6-17.
 */
public class RelativePerformanceParameters extends PerformanceParameters{
  @JsonRawValue
  private String baseline;
  private Relative relative;

  public RelativePerformanceParameters(String baseline, Relative relative) {
    this.baseline = baseline;
    this.relative = relative;
  }

  @JsonRawValue
  public String getBaseline() {
    return baseline;
  }

  public Relative getRelative() {
    return relative;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RelativePerformanceParameters that = (RelativePerformanceParameters) o;

    if (!baseline.equals(that.baseline)) return false;
    return relative.equals(that.relative);
  }

  @Override
  public int hashCode() {
    int result = baseline.hashCode();
    result = 31 * result + relative.hashCode();
    return result;
  }
}
