package org.drugis.addis.interventions.controller.command;

import org.drugis.addis.interventions.model.DoseConstraint;
import org.drugis.addis.interventions.model.InvalidConstraintException;
import org.drugis.addis.interventions.model.TitratedDoseIntervention;

import java.net.URI;

import static org.drugis.trialverse.util.Namespaces.CONCEPT_NAMESPACE;

/**
 * Created by daan on 5-4-16.
 */
public class TitratedInterventionCommand extends AbstractInterventionCommand {
  private ConstraintCommand titratedDoseMinConstraint;
  private ConstraintCommand titratedDoseMaxConstraint;

  public TitratedInterventionCommand() {
  }

  @Override
  public TitratedDoseIntervention toIntervention() throws InvalidConstraintException {
    DoseConstraint minConstraint = null;
    if (this.titratedDoseMinConstraint != null) {
      minConstraint = new DoseConstraint(this.titratedDoseMinConstraint.getLowerBound(), this.titratedDoseMinConstraint.getUpperBound());
    }
    DoseConstraint maxConstraint = null;
    if(this.titratedDoseMaxConstraint != null) {
      maxConstraint = new DoseConstraint(this.titratedDoseMaxConstraint.getLowerBound(), this.getTitratedDoseMaxConstraint().getUpperBound());
    }
    return new TitratedDoseIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(),
            URI.create(CONCEPT_NAMESPACE + this.getSemanticInterventionUuid()),
            this.getSemanticInterventionLabel(),
            minConstraint,
            maxConstraint);
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
