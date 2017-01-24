package org.drugis.addis.statistics.model;

/**
 * Created by joris on 24-1-17.
 */
public abstract class AbstractRelativeEffect {
  public abstract Distribution getDistribution();

  public abstract Double getNeutralValue();
}
