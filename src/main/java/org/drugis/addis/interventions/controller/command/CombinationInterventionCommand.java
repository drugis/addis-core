package org.drugis.addis.interventions.controller.command;

import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by connor on 31-5-16.
 */
public class CombinationInterventionCommand extends AbstractInterventionCommand {

  private Set<Integer> singleInterventionIds = new HashSet<>();

  public CombinationInterventionCommand() {
  }

  public CombinationInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, Set<Integer> singleInterventionIds) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.singleInterventionIds = singleInterventionIds;
  }

  public Set<Integer> getSingleInterventionIds() {
    return singleInterventionIds;
  }

  @Override
  public CombinationIntervention toIntervention() throws InvalidConstraintException {
    return new CombinationIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(), singleInterventionIds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationInterventionCommand that = (CombinationInterventionCommand) o;

    return singleInterventionIds.equals(that.singleInterventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + singleInterventionIds.hashCode();
    return result;
  }
}
