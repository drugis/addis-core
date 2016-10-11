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

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public AbstractIntervention toIntervention() throws InvalidConstraintException {
    return new InterventionSet(null, this.getProjectId(), this.getName(), this.getMotivation(), this.getInterventionIds());
  }
}
