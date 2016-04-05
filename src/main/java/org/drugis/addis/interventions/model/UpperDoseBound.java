package org.drugis.addis.interventions.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class UpperDoseBound {
  @Enumerated(EnumType.STRING)
  @Column(name = "upperBoundType")
  UpperBoundType type;

  @Column(name = "upperBoundValue")
  Double value;
  @Column(name = "upperBoundUnit")
  String unit;

  public UpperDoseBound() {
  }

  public UpperDoseBound(UpperBoundType type, Double value, String unit) {
    this.type = type;
    this.value = value;
    this.unit = unit;
  }

  public UpperBoundType getType() {
    return type;
  }

  public Double getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpperDoseBound that = (UpperDoseBound) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;
    return unit.equals(that.unit);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + unit.hashCode();
    return result;
  }
}
