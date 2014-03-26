package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by daan on 3/26/14.
 */
public enum MeasurementType {
  CONTINUOUS,
  RATE,
  CATEGORICAL;

  public String toString() {
    switch (this) {
      case CONTINUOUS: return "Continuous";
      case RATE: return "Rate";
      case CATEGORICAL: return "Categorical";
      default: return name();
    }
  };

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static MeasurementType fromJson(String text) {
    return valueOf(text.toUpperCase());
  }
}
