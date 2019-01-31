package org.drugis.addis.problems.service.model;

import java.util.Objects;

public class ContrastPerformanceParameters extends PerformanceParameters {
  private ContrastBaseline baseline;
  private Relative relative;

  public ContrastPerformanceParameters(ContrastBaseline baseline, Relative relative) {
    this.baseline = baseline;
    this.relative = relative;
  }

  public ContrastBaseline getBaseline() {
    return baseline;
  }

  public Relative getRelative() {
    return relative;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContrastPerformanceParameters that = (ContrastPerformanceParameters) o;
    return Objects.equals(baseline, that.baseline) &&
            Objects.equals(relative, that.relative);
  }

  @Override
  public int hashCode() {

    return Objects.hash(baseline, relative);
  }
}
