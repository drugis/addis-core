package org.drugis.addis.interventions.model;

/**
 * Created by daan on 1-4-16.
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BoundTypeDeserializer.class)
public enum BoundType {
  AT_LEAST("atLeast"),
  MORE_THAN("moreThan"),
  EXACTLY("exactly"),
  LESS_THAN("lessThan"),
  AT_MOST("atMost");

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
    } else if (inputString.equalsIgnoreCase("atLeast")) {
      return AT_LEAST;
    } else if (inputString.equalsIgnoreCase("moreThan")) {
      return MORE_THAN;
    } else if (inputString.equalsIgnoreCase("exactly")) {
      return EXACTLY;
    } else if (inputString.equalsIgnoreCase("lessThan")) {
      return LESS_THAN;
    } else if (inputString.equalsIgnoreCase("atMost")) {
      return AT_MOST;
    }
    throw new IllegalArgumentException("Can not create BoundType enum from empty String");
  }
}
