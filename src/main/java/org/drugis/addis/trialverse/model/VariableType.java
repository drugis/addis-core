package org.drugis.addis.trialverse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by daan on 3/26/14.
 */
public enum VariableType {
  POPULATION_CHARACTERISTIC,
  ENDPOINT,
  ADVERSE_EVENT;

  @Override
  public String toString() {
    switch (this) {
      case POPULATION_CHARACTERISTIC: return "Population Characteristic";
      case ENDPOINT: return "Endpoint";
      case ADVERSE_EVENT: return "Adverse Event";
      default: return name();
    }
  }

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }

  @JsonCreator
  public static VariableType fromJson(String text) {
    return valueOf(text.toUpperCase());
  }
}
