package org.drugis.addis.statistics.model;

/**
 * Created by joris on 24-1-17.
 */
public class TransformedStudentT extends TransformedStudentTBase {
  TransformedStudentT(double mu, double sigma, int degreesOfFreedom) {
    super(mu, sigma, degreesOfFreedom);
  }

  public Double getQuantile(double p) {
    return calculateQuantile(p);
  }

  @Override
  public Double getCumulativeProbability(Double x) {
    return calculateCumulativeProbability(x);
  }
}
