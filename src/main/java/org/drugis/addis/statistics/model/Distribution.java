package org.drugis.addis.statistics.model;

/**
 * Created by joris on 24-1-17.
 */
public interface Distribution {
  Double getQuantile(double p);

  Double getCumulativeProbability(Double neutralValue);
}
