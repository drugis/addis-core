package org.drugis.addis.trialverse.model.trialdata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Created by connor on 8-4-16.
 */
public class Dose {

  private Double value;
  private String periodicity;
  private URI unitConceptUri;
  private String unitLabel;
  private Double unitMultiplier;

  public Dose(Double value, String periodicity, URI unitConceptUri, String unitLabel, Double unitMultiplier) {
    this.value = value;
    this.periodicity = periodicity;
    this.unitConceptUri = unitConceptUri;
    this.unitLabel = unitLabel;
    this.unitMultiplier = unitMultiplier;
  }

  public Double getValue() {
    return value;
  }

  @JsonIgnore
  public BigDecimal getScaledValue() {
    return new BigDecimal(this.value).multiply(new BigDecimal(this.unitMultiplier == null ? 1.0 : this.unitMultiplier));
  }

  public String getPeriodicity() {
    return periodicity;
  }

  public URI getUnitConceptUri() {
    return unitConceptUri;
  }

  public String getUnitLabel() {
    return unitLabel;
  }

  public Double getUnitMultiplier() {
    return unitMultiplier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Dose dose = (Dose) o;

    if (value != null ? !value.equals(dose.value) : dose.value != null) return false;
    if (periodicity != null ? !periodicity.equals(dose.periodicity) : dose.periodicity != null) return false;
    if (unitConceptUri != null ? !unitConceptUri.equals(dose.unitConceptUri) : dose.unitConceptUri != null)
      return false;
    if (unitLabel != null ? !unitLabel.equals(dose.unitLabel) : dose.unitLabel != null) return false;
    return unitMultiplier != null ? unitMultiplier.equals(dose.unitMultiplier) : dose.unitMultiplier == null;

  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (periodicity != null ? periodicity.hashCode() : 0);
    result = 31 * result + (unitConceptUri != null ? unitConceptUri.hashCode() : 0);
    result = 31 * result + (unitLabel != null ? unitLabel.hashCode() : 0);
    result = 31 * result + (unitMultiplier != null ? unitMultiplier.hashCode() : 0);
    return result;
  }
}
