package org.drugis.addis.interventions.controller.command;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.InterventionSet;
import org.drugis.addis.interventions.model.InvalidConstraintException;

import java.util.Set;

/**
 * Created by daan on 11-10-16.
 */
public class InterventionSetCommand extends AbstractInterventionCommand {

  private Set<Integer> interventionIds;

  public InterventionSetCommand(){}

  public InterventionSetCommand(Integer projectId, String name, String motivation, Set<Integer> interventionIds) {
    super(projectId, name, motivation, null, null);
    this.interventionIds = interventionIds;
  }

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public AbstractIntervention toIntervention() throws InvalidConstraintException {
    return new InterventionSet(null, this.getProjectId(), this.getName(), this.getMotivation(), this.getInterventionIds());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    InterventionSetCommand that = (InterventionSetCommand) o;

    return interventionIds.equals(that.interventionIds);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}
