package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.SingleIntervention;

/**
 * Created by connor on 1-6-16.
 */
public class SingleInterventionViewAdapter extends AbstractInterventionViewAdapter {

  private String semanticInterventionUri;
  private String semanticInterventionLabel;

  protected SingleInterventionViewAdapter(SingleIntervention intervention) {
    super(intervention);
    this.semanticInterventionUri = intervention.getSemanticInterventionUri().toString();
    this.semanticInterventionLabel = intervention.getSemanticInterventionLabel();
  }

  public String getSemanticInterventionUri() {
    return semanticInterventionUri;
  }

  public String getSemanticInterventionLabel() {
    return semanticInterventionLabel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SingleInterventionViewAdapter that = (SingleInterventionViewAdapter) o;

    if (!semanticInterventionUri.equals(that.semanticInterventionUri)) return false;
    return semanticInterventionLabel.equals(that.semanticInterventionLabel);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + semanticInterventionUri.hashCode();
    result = 31 * result + semanticInterventionLabel.hashCode();
    return result;
  }
}
