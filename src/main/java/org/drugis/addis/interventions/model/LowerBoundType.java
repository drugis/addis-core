package org.drugis.addis.interventions.model;

import java.math.BigDecimal;

/**
 * Created by daan on 5-4-16.
 */

public enum LowerBoundType {
  AT_LEAST, MORE_THAN, EXACTLY;

  public boolean isValidForBound(BigDecimal value, BigDecimal boundValue){
    switch(this) {
      case AT_LEAST:
        return value.compareTo(boundValue) >= 0;
      case MORE_THAN:
        return value.compareTo(boundValue) > 0;
      case EXACTLY:
        return boundValue.equals(value);
    }
    return false;
  }
}