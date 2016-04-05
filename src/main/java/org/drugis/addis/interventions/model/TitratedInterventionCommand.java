package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class TitratedInterventionCommand extends AbstractInterventionCommand {
  ConstraintCommand titratedDoseMinConstraint;
  ConstraintCommand titratedDoseMaxConstraint;

  public TitratedInterventionCommand() {
  }

  public TitratedInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, ConstraintCommand titratedDoseMinConstraint, ConstraintCommand titratedDoseMaxConstraint) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.titratedDoseMinConstraint = titratedDoseMinConstraint;
    this.titratedDoseMaxConstraint = titratedDoseMaxConstraint;
  }

  public ConstraintCommand getTitratedDoseMinConstraint() {
    return titratedDoseMinConstraint;
  }

  public ConstraintCommand getTitratedDoseMaxConstraint() {
    return titratedDoseMaxConstraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TitratedInterventionCommand that = (TitratedInterventionCommand) o;

    if (titratedDoseMinConstraint != null ? !titratedDoseMinConstraint.equals(that.titratedDoseMinConstraint) : that.titratedDoseMinConstraint != null)
      return false;
    return titratedDoseMaxConstraint != null ? titratedDoseMaxConstraint.equals(that.titratedDoseMaxConstraint) : that.titratedDoseMaxConstraint == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (titratedDoseMinConstraint != null ? titratedDoseMinConstraint.hashCode() : 0);
    result = 31 * result + (titratedDoseMaxConstraint != null ? titratedDoseMaxConstraint.hashCode() : 0);
    return result;
  }
}
