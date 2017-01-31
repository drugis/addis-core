package org.drugis.addis.statistics.model;

import org.drugis.addis.statistics.exception.MissingMeasurementException;

/**
 * Created by joris on 24-1-17.
 */
public abstract class AbstractRelativeEffect {
  public abstract Distribution getDistribution() throws MissingMeasurementException;

  public abstract Double getNeutralValue();
}
