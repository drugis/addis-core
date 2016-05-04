package org.drugis.addis.interventions.model;

/**
 * Created by daan on 1-4-16.
 */

public enum UpperBoundType {
  LESS_THAN, AT_MOST;

  public boolean isValidForBound(Double value, Double boundValue) {
    switch (this) {
      case LESS_THAN:
        return value < boundValue;
      case AT_MOST:
        return value <= boundValue;
      default:
        return false;
    }
  }
}
