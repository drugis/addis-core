package org.drugis.addis.interventions;

import java.net.URI;

/**
 * Created by joris on 11-4-17.
 */
public class InterventionMultiplierCommand {
  private String unitName;
  private URI unitConcept;
  private Double conversionMultiplier;

  public InterventionMultiplierCommand() {
  }

  public String getUnitName() {
    return unitName;
  }

  public URI getUnitConcept() {
    return unitConcept;
  }

  public Double getConversionMultiplier() {
    return conversionMultiplier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InterventionMultiplierCommand that = (InterventionMultiplierCommand) o;

    if (!unitName.equals(that.unitName)) return false;
    if (!unitConcept.equals(that.unitConcept)) return false;
    return conversionMultiplier.equals(that.conversionMultiplier);
  }

  @Override
  public int hashCode() {
    int result = unitName.hashCode();
    result = 31 * result + unitConcept.hashCode();
    result = 31 * result + conversionMultiplier.hashCode();
    return result;
  }
}
