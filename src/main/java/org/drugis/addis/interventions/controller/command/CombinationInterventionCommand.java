package org.drugis.addis.interventions.controller.command;

import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by connor on 31-5-16.
 */
public class CombinationInterventionCommand extends AbstractInterventionCommand {

  private Set<Integer> interventionIds = new HashSet<>();

  public CombinationInterventionCommand() {
  }

  public CombinationInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, Set<Integer> interventionIds) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.interventionIds = interventionIds;
  }

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public CombinationIntervention toIntervention() throws InvalidConstraintException {
    return new CombinationIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(), this.getInterventionIds());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationInterventionCommand that = (CombinationInterventionCommand) o;

    return interventionIds.equals(that.interventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}
