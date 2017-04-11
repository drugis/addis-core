package org.drugis.addis.interventions.controller.command;

import java.net.URI;

/**
 * Created by connor on 13-4-16.
 */
public abstract class AbstractBoundCommand {
  private String unitName;
  private String unitPeriod;
  private URI unitConcept;
  private Double value;
  private Double conversionMultplier;

  public AbstractBoundCommand() {}

  public AbstractBoundCommand(Double value, String unitName, String unitPeriod, URI unitConcept) {
    this.value = value;
    this.unitName = unitName;
    this.unitPeriod = unitPeriod;
    this.unitConcept = unitConcept;
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

  public URI getUnitConcept() {
    return unitConcept;
  }

  public Double getConversionMultplier() {
    return conversionMultplier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractBoundCommand that = (AbstractBoundCommand) o;

    if (!unitName.equals(that.unitName)) return false;
    if (!unitPeriod.equals(that.unitPeriod)) return false;
    if (!unitConcept.equals(that.unitConcept)) return false;
    if (!value.equals(that.value)) return false;
    return conversionMultplier != null ? conversionMultplier.equals(that.conversionMultplier) : that.conversionMultplier == null;
  }

  @Override
  public int hashCode() {
    int result = unitName.hashCode();
    result = 31 * result + unitPeriod.hashCode();
    result = 31 * result + unitConcept.hashCode();
    result = 31 * result + value.hashCode();
    result = 31 * result + (conversionMultplier != null ? conversionMultplier.hashCode() : 0);
    return result;
  }
}

