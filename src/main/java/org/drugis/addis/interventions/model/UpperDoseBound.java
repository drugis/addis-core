package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;

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

  public UpperDoseBound(UpperBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept, Double conversionMultiplier) throws InvalidConstraintException {
    if(type==null || value == null || unitName == null || unitPeriod == null){
      throw new InvalidConstraintException("LowerBound contains null fields");
    }
    this.type = type;
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    if(unitConcept != null) {
      this.unitConcept = unitConcept.toString();
    }
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
    if(unitConcept == null){
      return null;
    }
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
    return type == that.type &&
            Objects.equals(value, that.value) &&
            Objects.equals(unitName, that.unitName) &&
            Objects.equals(unitPeriod, that.unitPeriod) &&
            Objects.equals(unitConcept, that.unitConcept) &&
            Objects.equals(conversionMultiplier, that.conversionMultiplier);
  }

  @Override
  public int hashCode() {

    return Objects.hash(type, value, unitName, unitPeriod, unitConcept, conversionMultiplier);
  }
}
