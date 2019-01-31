package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.Objects;

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
    return Objects.equals(drugInstance, that.drugInstance) &&
            Objects.equals(drugConcept, that.drugConcept);
  }

  @Override
  public int hashCode() {

    return Objects.hash(drugInstance, drugConcept);
  }
}
