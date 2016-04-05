package org.drugis.addis.interventions.model;

/**
 * Created by daan on 5-4-16.
 */
public class SimpleInterventionCommand extends AbstractInterventionCommand {
  public SimpleInterventionCommand() {
  }

  public SimpleInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionUuid, String semanticInterventionLabel) {
    super(projectId, name, motivation, semanticInterventionLabel, semanticInterventionUuid);
  }
}
