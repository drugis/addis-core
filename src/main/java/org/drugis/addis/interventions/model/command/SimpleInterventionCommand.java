package org.drugis.addis.interventions.model.command;

import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;

import java.net.URI;

import static org.drugis.trialverse.util.Namespaces.CONCEPT_NAMESPACE;

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
            new SemanticInterventionUriAndName(
                    URI.create(CONCEPT_NAMESPACE + super.getSemanticInterventionUuid()),
                    super.getSemanticInterventionLabel()
            )
    );
  }
}
