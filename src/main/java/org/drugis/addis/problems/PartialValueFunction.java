package org.drugis.addis.problems;

import java.util.Arrays;

/**
 * Created by connor on 25-3-14.
 */
public class PartialValueFunction {
  private String direction;
  private String type;
  private Double[] range;

  public PartialValueFunction(String direction, String type, Double[] range) {
    this.direction = direction;
    this.type = type;
    this.range = range;
  }

  public String getDirection() {
    return direction;
  }

  public String getType() {
    return type;
  }

  public Double[] getRange() {
    return range;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartialValueFunction)) return false;

    PartialValueFunction that = (PartialValueFunction) o;

    if (!direction.equals(that.direction)) return false;
    if (!Arrays.equals(range, that.range)) return false;
    if (!type.equals(that.type)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = direction.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + (range != null ? Arrays.hashCode(range) : 0);
    return result;
  }
}
