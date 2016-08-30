package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by daan on 18-8-16.
 */
public enum BiasDirection {
  T_1("t1"),
  T_2("t2");

  private String value;

  BiasDirection(String value) {
    this.value = value;
  }

  @JsonCreator
  public BiasDirection fromJson(String value) {
    switch(value) {
      case "t1": return T_1;
      case "t2": return T_2;
      default: throw new IllegalArgumentException("Invalid bias direction: " + value);
    }
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
