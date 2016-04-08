package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 8-4-16.
 */
public class AbstractSemanticIntervention {
  private URI drugInstance;
  private URI drugConcept;

  public AbstractSemanticIntervention() {
  }

  public AbstractSemanticIntervention(URI drugInstance, URI drugConcept) {
    this.drugInstance = drugInstance;
    this.drugConcept = drugConcept;
  }

  public URI getDrugInstance() {
    return drugInstance;
  }

  public URI getDrugConcept() {
    return drugConcept;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractSemanticIntervention that = (AbstractSemanticIntervention) o;

    if (!drugInstance.equals(that.drugInstance)) return false;
    return drugConcept.equals(that.drugConcept);

  }

  @Override
  public int hashCode() {
    int result = drugInstance.hashCode();
    result = 31 * result + drugConcept.hashCode();
    return result;
  }
}
