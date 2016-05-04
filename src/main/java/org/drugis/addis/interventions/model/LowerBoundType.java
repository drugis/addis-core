package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */

public enum LowerBoundType {
  AT_LEAST, MORE_THAN, EXACTLY;

  public boolean isValidForBound(Double value, Double boundValue){
    switch(this) {
      case AT_LEAST:
        return value >= boundValue;
      case MORE_THAN:
        return value > boundValue;
      case EXACTLY:
        return boundValue.equals(value);
    }
    return false;
  }
}