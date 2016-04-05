package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class TitratedDoseIntervention extends AbstractIntervention {
  private DoseConstraint minConstraint;
  private DoseConstraint maxConstraint;

  public TitratedDoseIntervention() {
  }

  public TitratedDoseIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
    this.minConstraint = minConstraint;
    this.maxConstraint = maxConstraint;
  }
}
