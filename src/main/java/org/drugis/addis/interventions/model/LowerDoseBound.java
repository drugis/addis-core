package org.drugis.addis.interventions.model;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class LowerDoseBound {
  @Enumerated(EnumType.STRING)
  private LowerBoundType type;

  private Double value;

  private String unitName;

  private String unitPeriod;

  public LowerDoseBound() {
  }

  public LowerDoseBound(LowerBoundType type, Double value, String unitName, String unitPeriod) {
    this.type = type;
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
  }

  public LowerBoundType getType() {
    return type;
  }

  public Double getValue() {
    return value;
  }

  public String getUnitName() {
    return unitName;
  }

  public String getUnitPeriod() {
    return unitPeriod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LowerDoseBound that = (LowerDoseBound) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;
    if (!unitName.equals(that.unitName)) return false;
    return unitPeriod.equals(that.unitPeriod);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + unitName.hashCode();
    result = 31 * result + unitPeriod.hashCode();
    return result;
  }
}
