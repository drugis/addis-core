package org.drugis.addis.problems.service.model;

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

    if (!type.equals(that.type)) return false;
    return parameters.equals(that.parameters);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + parameters.hashCode();
    return result;
  }
}
