package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class UpperDoseBound {
  @Enumerated(EnumType.STRING)
  private UpperBoundType type;

  private Double value;
  private String unitName;
  private String unitPeriod;
  private String unitConcept;
  private Double conversionMultiplier;

  public UpperDoseBound() {
  }

  public UpperDoseBound(UpperBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept, Double conversionMultiplier) {
    this.type = type;
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    this.unitConcept = unitConcept.toString();
    this.conversionMultiplier = conversionMultiplier;
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

  public Double getConversionMultiplier() {
    return conversionMultiplier;
  }

  @JsonIgnore
  public BigDecimal getScaledValue() {
    BigDecimal bigDecimalValue = new BigDecimal(value.toString());
    return conversionMultiplier != null ? bigDecimalValue.multiply(new BigDecimal(conversionMultiplier.toString())) : bigDecimalValue;
  }

  public void setConversionMultiplier(Double conversionMultiplier) {
    this.conversionMultiplier = conversionMultiplier;
  }

  public Boolean isMatched(URI unitConcept, String unitName) {
    return this.unitConcept.equals(unitConcept.toString()) && this.unitName.equals(unitName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpperDoseBound that = (UpperDoseBound) o;

    if (type != that.type) return false;
    if (!value.equals(that.value)) return false;
    if (!unitName.equals(that.unitName)) return false;
    if (!unitPeriod.equals(that.unitPeriod)) return false;
    if (!unitConcept.equals(that.unitConcept)) return false;
    return conversionMultiplier != null ? conversionMultiplier.equals(that.conversionMultiplier) : that.conversionMultiplier == null;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + unitName.hashCode();
    result = 31 * result + unitPeriod.hashCode();
    result = 31 * result + unitConcept.hashCode();
    result = 31 * result + (conversionMultiplier != null ? conversionMultiplier.hashCode() : 0);
    return result;
  }
}
