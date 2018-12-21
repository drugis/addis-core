package org.drugis.addis.problems.service.model;

import java.util.Objects;

public class ContrastPerformance extends AbstractPerformance {

  private String type;
  private ContrastPerformanceParameters parameters;

  public ContrastPerformance(String type, ContrastPerformanceParameters parameters) {
    this.type = type;
    this.parameters = parameters;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public ContrastPerformanceParameters getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContrastPerformance that = (ContrastPerformance) o;
    return Objects.equals(type, that.type) &&
            Objects.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {

    return Objects.hash(type, parameters);
  }
}
