package org.drugis.addis.interventions.model;

import org.drugis.trialverse.util.Namespaces;

import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
public class FixedInterventionCommand extends AbstractInterventionCommand {
  ConstraintCommand fixedDoseConstraintCommand;

  public FixedInterventionCommand() {
  }

  @Override
  public FixedDoseIntervention toIntervention() {
    return new FixedDoseIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(),
            URI.create(Namespaces.CONCEPT_NAMESPACE + this.getSemanticInterventionUuid()),
            this.getSemanticInterventionLabel(),
            new DoseConstraint(this.fixedDoseConstraintCommand.getLowerBound(), this.fixedDoseConstraintCommand.getUpperBound()));
  }

  public FixedInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, ConstraintCommand fixedDoseConstraintCommand) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.fixedDoseConstraintCommand = fixedDoseConstraintCommand;
  }

  public ConstraintCommand getFixedDoseConstraintCommand() {
    return fixedDoseConstraintCommand;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    FixedInterventionCommand that = (FixedInterventionCommand) o;

    return fixedDoseConstraintCommand.equals(that.fixedDoseConstraintCommand);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + fixedDoseConstraintCommand.hashCode();
    return result;
  }
}
