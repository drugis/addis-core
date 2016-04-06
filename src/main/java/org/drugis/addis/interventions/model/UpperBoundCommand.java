package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class UpperBoundCommand {
  private UpperBoundType type;
  private String unitName;
  private String unitPeriod;
  private Double value;

  public UpperBoundCommand() {
  }

  public UpperBoundCommand(UpperBoundType type, Double value, String unitName, String unitPeriod) {
    this.type = type;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    this.value = value;
  }

  public UpperBoundType getType() {
    return type;
  }

  public String getUnitName() {
    return unitName;
  }

  public Double getValue() {
    return value;
  }

  public String getUnitPeriod() {
    return unitPeriod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpperBoundCommand that = (UpperBoundCommand) o;

    if (type != that.type) return false;
    if (!unitName.equals(that.unitName)) return false;
    if (!unitPeriod.equals(that.unitPeriod)) return false;
    return value.equals(that.value);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + unitName.hashCode();
    result = 31 * result + unitPeriod.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
