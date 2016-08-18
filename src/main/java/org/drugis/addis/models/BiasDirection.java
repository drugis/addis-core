package org.drugis.addis.models;

/**
 * Created by daan on 18-8-16.
 */
public enum BiasDirection {
  T_1(1),
  T_2(2);

  private int value;

  BiasDirection(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
