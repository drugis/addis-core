package org.drugis.addis.interventions.model;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class UpperDoseBound {
  @Enumerated(EnumType.STRING)
  UpperBoundType type;

  Double value;
  String unitName;
  String unitPeriod;
  String unitConcept;

  public UpperDoseBound() {
  }

  public UpperDoseBound(UpperBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept) {
    this.type = type;
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    this.unitConcept = unitConcept.toString();
  }

  public UpperBoundType getType() {
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

  public URI getUnitConcept() {
    return URI.create(unitConcept);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpperDoseBound that = (UpperDoseBound) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;
    return unitName.equals(that.unitName);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + unitName.hashCode();
    return result;
  }
}
