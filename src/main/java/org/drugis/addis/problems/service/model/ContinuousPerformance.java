package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/27/14.
 */
public class ContinuousPerformance extends AbstractPerformance {

  public static final String DNORM = "dnorm";

  private ContinuousPerformanceParameters parameters;

  public ContinuousPerformance(ContinuousPerformanceParameters parameters) {
    super();
    this.parameters = parameters;
  }

  @Override
  public String getType() {
    return DNORM;
  }

  @Override
  public ContinuousPerformanceParameters getParameters() {
    return this.parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ContinuousPerformance that = (ContinuousPerformance) o;

    if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return parameters != null ? parameters.hashCode() : 0;
  }
}
