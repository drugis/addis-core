package org.drugis.addis.problems.model;

/**
 * Created by joris on 14-3-17.
 */
public class StudentTBaselineDistribution extends AbstractBaselineDistribution {
  private Double mu;
  private Double stdErr;
  private Integer dof;

  public StudentTBaselineDistribution() {
  }

  public StudentTBaselineDistribution(String name, String type, String scale, Double mu, Double stdErr, Integer dof) {
    super(name, type, scale);
    this.mu = mu;
    this.stdErr = stdErr;
    this.dof = dof;
  }

  public Integer getDof() {
    return dof;
  }

  public Double getMu() {
    return mu;
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

    if (!mu.equals(that.mu)) return false;
    if (!stdErr.equals(that.stdErr)) return false;
    return dof.equals(that.dof);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mu.hashCode();
    result = 31 * result + stdErr.hashCode();
    result = 31 * result + dof.hashCode();
    return result;
  }
}
