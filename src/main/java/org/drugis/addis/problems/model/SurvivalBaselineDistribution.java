package org.drugis.addis.problems.model;

public class SurvivalBaselineDistribution extends AbstractBaselineDistribution {
  private Double alpha;
  private Double beta;
  private String summaryMeasure;
  private Double time;

  public SurvivalBaselineDistribution() {
    super();
  }

  public SurvivalBaselineDistribution(String name, String type, String scale, Double alpha, Double beta, String summaryMeasure, Double time) {
    super(name, type, scale);
    this.alpha = alpha;
    this.beta = beta;
    this.summaryMeasure = summaryMeasure;
    this.time = time;
  }

  public Double getAlpha() {
    return alpha;
  }

  public Double getBeta() {
    return beta;
  }

  public String getSummaryMeasure() {
    return summaryMeasure;
  }

  public Double getTime() {
    return time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SurvivalBaselineDistribution that = (SurvivalBaselineDistribution) o;

    if (alpha != null ? !alpha.equals(that.alpha) : that.alpha != null) return false;
    if (beta != null ? !beta.equals(that.beta) : that.beta != null) return false;
    if (summaryMeasure != null ? !summaryMeasure.equals(that.summaryMeasure) : that.summaryMeasure != null)
      return false;
    return time != null ? time.equals(that.time) : that.time == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (alpha != null ? alpha.hashCode() : 0);
    result = 31 * result + (beta != null ? beta.hashCode() : 0);
    result = 31 * result + (summaryMeasure != null ? summaryMeasure.hashCode() : 0);
    result = 31 * result + (time != null ? time.hashCode() : 0);
    return result;
  }
}
