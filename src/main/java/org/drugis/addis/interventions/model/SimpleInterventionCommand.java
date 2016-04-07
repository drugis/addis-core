package org.drugis.addis.interventions.model;

import org.drugis.addis.trialverse.model.SemanticIntervention;

/**
 * Created by daan on 5-4-16.
 */
public class SimpleInterventionCommand extends AbstractInterventionCommand {
  public SimpleInterventionCommand() {
  }

  public SimpleInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionUuid, String semanticInterventionLabel) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
  }

  @Override
  public SimpleIntervention toIntervention() {
    return new SimpleIntervention(
            super.getProjectId(),
            super.getName(),
            super.getMotivation(),
            new SemanticIntervention(
                    super.getSemanticInterventionUuid(),
                    super.getSemanticInterventionLabel()
            )
    );
  }
}
