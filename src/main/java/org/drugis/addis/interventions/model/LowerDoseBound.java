package org.drugis.addis.interventions.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class LowerDoseBound {
  @Enumerated(EnumType.STRING)
  @Column(name = "lowerBoundType")
  private LowerBoundType type;

  @Column(name = "lowerBoundValue")
  private Double value;

  @Column(name = "lowerBoundUnit")
  private String unit;

  public LowerDoseBound() {
  }

  public LowerDoseBound(LowerBoundType type, Double value, String unit) {
    this.type = type;
    this.value = value;
    this.unit = unit;
  }

  public LowerBoundType getType() {
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

    LowerDoseBound that = (LowerDoseBound) o;

    if (type != that.type) return false;
    if (value != null ? !value.equals(that.value) : that.value != null) return false;
    return unit != null ? unit.equals(that.unit) : that.unit == null;

  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (unit != null ? unit.hashCode() : 0);
    return result;
  }
}
