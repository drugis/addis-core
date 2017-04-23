package org.drugis.addis.scaledUnits;

import java.net.URI;

/**
 * Created by Daan on 23/04/2017.
 */
public class ScaledUnitCommand {
  private URI conceptUri;
  public Double multiplier;
  private String name;

  public ScaledUnitCommand() {
  }

  public ScaledUnitCommand(URI conceptUri, Double multiplier, String name) {
    this.conceptUri = conceptUri;
    this.multiplier = multiplier;
    this.name = name;
  }

  public URI getConceptUri() {
    return conceptUri;
  }

  public Double getMultiplier() {
    return multiplier;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ScaledUnitCommand that = (ScaledUnitCommand) o;

    if (!conceptUri.equals(that.conceptUri)) return false;
    if (!multiplier.equals(that.multiplier)) return false;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = conceptUri.hashCode();
    result = 31 * result + multiplier.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
