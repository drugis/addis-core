package org.drugis.addis.problems.model;

/**
 * Created by joris on 14-3-17.
 */
public class BetaCloglogBaselineDistribution extends AbstractBaselineDistribution {
  private Integer alpha;
  private Integer beta;

  public BetaCloglogBaselineDistribution() {
  }

  public BetaCloglogBaselineDistribution(String name, String type, String scale, Integer alpha, Integer beta) {
    super(name, type, scale);
    this.alpha = alpha;
    this.beta = beta;
  }

  public Integer getAlpha() {
    return alpha;
  }

  public Integer getBeta() {
    return beta;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    BetaCloglogBaselineDistribution that = (BetaCloglogBaselineDistribution) o;

    if (!alpha.equals(that.alpha)) return false;
    return beta.equals(that.beta);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + alpha.hashCode();
    result = 31 * result + beta.hashCode();
    return result;
  }
}
