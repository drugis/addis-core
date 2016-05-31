package org.drugis.addis.interventions.model.command;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by connor on 31-5-16.
 */
public class CombinationInterventionCommand extends AbstractInterventionCommand {

  private Set<AbstractInterventionCommand> interventions = new HashSet();

  public CombinationInterventionCommand() {
  }

  public CombinationInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid, Set<AbstractInterventionCommand> interventions) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
    this.interventions = interventions;
  }

  public Set<AbstractInterventionCommand> getInterventions() {
    return interventions;
  }

  @Override
  public CombinationIntervention toIntervention() throws InvalidConstraintException {

    Set<AbstractIntervention> interventionEntities = interventions
            .stream()
            .map(i -> {
              try {
                return i.toIntervention();
              } catch (InvalidConstraintException e) {
                e.printStackTrace();
              }
              return null;
            })
            .collect(Collectors.toSet());

    return new CombinationIntervention(null, this.getProjectId(), this.getName(), this.getMotivation(), interventionEntities);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationInterventionCommand that = (CombinationInterventionCommand) o;

    return interventions.equals(that.interventions);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventions.hashCode();
    return result;
  }
}
