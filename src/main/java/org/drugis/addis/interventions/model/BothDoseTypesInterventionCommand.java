package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class BothDoseTypesInterventionCommand extends AbstractInterventionCommand {
  ConstraintCommand bothDoseTypesMinConstraint;
  ConstraintCommand bothDoseTypesMaxConstraint;

  @Override
  public AbstractIntervention toIntervention() {
    return new BothDoseTypesIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(),
            this.getSemanticInterventionUuid(), this.getSemanticInterventionLabel(),
            new DoseConstraint(this.bothDoseTypesMinConstraint.getLowerBound(), this.bothDoseTypesMinConstraint.getUpperBound()),
            new DoseConstraint(this.bothDoseTypesMaxConstraint.getLowerBound(), this.bothDoseTypesMaxConstraint.getUpperBound()));
  }


}
