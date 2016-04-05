package org.drugis.addis.interventions.model;

/**
 * Created by daan on 1-4-16.
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BoundTypeDeserializer.class)
public enum BoundType {
  AT_LEAST("AT_LEAST"),
  MORE_THAN("MORE_THAN"),
  EXACTLY("EXACTLY"),
  LESS_THAN("LESS_THAN"),
  AT_MOST("AT_MOST");

  private String value;

  BoundType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static BoundType fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create BoundType enum from empty String");
    } else if (inputString.equalsIgnoreCase("AT_LEAST")) {
      return AT_LEAST;
    } else if (inputString.equalsIgnoreCase("MORE_THAN")) {
      return MORE_THAN;
    } else if (inputString.equalsIgnoreCase("EXACTLY")) {
      return EXACTLY;
    } else if (inputString.equalsIgnoreCase("LESS_THAN")) {
      return LESS_THAN;
    } else if (inputString.equalsIgnoreCase("AT_MOST")) {
      return AT_MOST;
    }
    throw new IllegalArgumentException("Can not create BoundType enum from " + inputString);
  }
}
