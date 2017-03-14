package org.drugis.addis.problems.model;

/**
 * Created by joris on 14-3-17.
 */
public class StudentTBaselineDistribution extends AbstractBaselineDistribution{
  private Double mean;
  private Double stdErr;

  public StudentTBaselineDistribution() {
  }

  public StudentTBaselineDistribution(String name, String type, String scale, Double mean, Double stdErr) {
    super(name, type, scale);
    this.mean = mean;
    this.stdErr = stdErr;
  }

  public Double getMean() {
    return mean;
  }

  public Double getStdErr() {
    return stdErr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    StudentTBaselineDistribution that = (StudentTBaselineDistribution) o;

    if (!mean.equals(that.mean)) return false;
    return stdErr.equals(that.stdErr);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mean.hashCode();
    result = 31 * result + stdErr.hashCode();
    return result;
  }
}
