package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class UpperBoundCommand {
  private UpperBoundType type;
  private String unit;
  private Double value;

  public UpperBoundCommand() {
  }

  public UpperBoundCommand(UpperBoundType type, String unit, Double value) {
    this.type = type;
    this.unit = unit;
    this.value = value;
  }

  public UpperBoundType getType() {
    return type;
  }

  public String getUnit() {
    return unit;
  }

  public Double getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpperBoundCommand that = (UpperBoundCommand) o;

    if (type != that.type) return false;
    if (!unit.equals(that.unit)) return false;
    return value.equals(that.value);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + unit.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
