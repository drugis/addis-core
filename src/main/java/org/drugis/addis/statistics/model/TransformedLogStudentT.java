package org.drugis.addis.statistics.model;

/**
 * Created by joris on 24-1-17.
 */
public class TransformedLogStudentT extends TransformedStudentTBase {
  public TransformedLogStudentT(Double mu, Double sigma, Integer degreesOfFreedom)  {
    super(mu, sigma,degreesOfFreedom);
  }

  public Double getQuantile(double p) {
    return Math.exp(calculateQuantile(p));
  }

  @Override
  public Double getCumulativeProbability(Double x) {
    return calculateCumulativeProbability(Math.log(x));
  }
}
