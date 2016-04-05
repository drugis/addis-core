package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class FixedInterventionCommand extends AbstractInterventionCommand {
  ConstraintCommand fixedDoseConstraint;

  public FixedInterventionCommand() {
  }

  public FixedInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, ConstraintCommand fixedDoseConstraint) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.fixedDoseConstraint = fixedDoseConstraint;
  }

  public ConstraintCommand getFixedDoseConstraint() {
    return fixedDoseConstraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    FixedInterventionCommand that = (FixedInterventionCommand) o;

    return fixedDoseConstraint.equals(that.fixedDoseConstraint);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + fixedDoseConstraint.hashCode();
    return result;
  }
}
