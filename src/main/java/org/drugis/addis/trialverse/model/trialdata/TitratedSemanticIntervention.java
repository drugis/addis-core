package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;

/**
 * Created by connor on 8-4-16.
 */
public class TitratedSemanticIntervention extends AbstractSemanticIntervention {

  private Dose minDose;
  private Dose maxDose;

  public TitratedSemanticIntervention(URI drugInstance, URI drugConcept, Dose minDose, Dose maxDose) {
    super(drugInstance, drugConcept);
    this.minDose = minDose;
    this.maxDose = maxDose;
  }

  public Dose getMaxDose() {
    return maxDose;
  }

  public Dose getMinDose() {
    return minDose;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TitratedSemanticIntervention that = (TitratedSemanticIntervention) o;

    if (maxDose != null ? !maxDose.equals(that.maxDose) : that.maxDose != null) return false;
    return minDose != null ? minDose.equals(that.minDose) : that.minDose == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (maxDose != null ? maxDose.hashCode() : 0);
    result = 31 * result + (minDose != null ? minDose.hashCode() : 0);
    return result;
  }
}
