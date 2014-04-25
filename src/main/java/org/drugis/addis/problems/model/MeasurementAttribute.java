package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by daan on 3/26/14.
 */
public enum MeasurementAttribute {
  RATE, SAMPLE_SIZE, MEAN, STANDARD_DEVIATION;

  public String toString() {
    switch (this) {
      case RATE:
        return "rate";
      case SAMPLE_SIZE:
        return "sample size";
      case MEAN:
        return "mean";
      case STANDARD_DEVIATION:
        return "standard deviation";
      default:
        return name();
    }
  }

  ;

  @JsonValue
  public String toJson() {
    return this.toString();
  }

  @JsonCreator
  public static MeasurementAttribute fromJson(String text) {
    switch (text) {
      case "rate":
        return RATE;
      case "sample size":
        return SAMPLE_SIZE;
      case "mean":
        return MEAN;
      case "standard deviation":
        return STANDARD_DEVIATION;
    }
    throw new EnumConstantNotPresentException(MeasurementType.class, text);
  }
}
