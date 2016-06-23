package org.drugis.addis.interventions.model;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.net.URI;

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

  private String unitConcept;

  public LowerDoseBound() {
  }

  public LowerDoseBound(LowerBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept) {
    this.type = type;
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    this.unitConcept = unitConcept.toString();
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

  public URI getUnitConcept() {
    return URI.create(unitConcept);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LowerDoseBound that = (LowerDoseBound) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;
    if (!unitName.equals(that.unitName)) return false;
    if (!unitPeriod.equals(that.unitPeriod)) return false;
    return unitConcept.equals(that.unitConcept);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + unitName.hashCode();
    result = 31 * result + unitPeriod.hashCode();
    result = 31 * result + unitConcept.hashCode();
    return result;
  }
}
