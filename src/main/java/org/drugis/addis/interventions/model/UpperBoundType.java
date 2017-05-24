package org.drugis.addis.interventions.model;

import java.math.BigDecimal;

/**
 * Created by daan on 1-4-16.
 */

public enum UpperBoundType {
  LESS_THAN, AT_MOST;

  public boolean isValidForBound(BigDecimal value, BigDecimal boundValue) {
    switch (this) {
      case LESS_THAN:
        return value.compareTo(boundValue) < 0;
      case AT_MOST:
        return value.compareTo(boundValue) <= 0;
      default:
        return false;
    }
  }
}
