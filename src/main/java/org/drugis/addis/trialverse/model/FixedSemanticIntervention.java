package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 8-4-16.
 */
public class FixedSemanticIntervention extends AbstractSemanticIntervention {

  private Dose dose;

  public FixedSemanticIntervention(URI drugInstance, URI drugConcept, Dose dose) {
    super(drugInstance, drugConcept);
    this.dose = dose;
  }

  public Dose getDose() {
    return dose;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    FixedSemanticIntervention that = (FixedSemanticIntervention) o;

    return dose != null ? dose.equals(that.dose) : that.dose == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (dose != null ? dose.hashCode() : 0);
    return result;
  }
}
