package org.drugis.addis.problems.service.model;

import java.util.Objects;

/**
 * Created by joris on 14-6-17.
 */
public class RelativePerformance extends AbstractPerformance{
  private String type;
  private RelativePerformanceParameters parameters;

  public RelativePerformance(String type, RelativePerformanceParameters parameters) {
    this.type = type;
    this.parameters = parameters;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public PerformanceParameters getParameters() {
    return parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RelativePerformance that = (RelativePerformance) o;
    return Objects.equals(type, that.type) &&
            Objects.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {

    return Objects.hash(type, parameters);
  }
}
