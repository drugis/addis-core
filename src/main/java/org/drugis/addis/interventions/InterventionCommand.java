package org.drugis.addis.interventions;

import org.drugis.addis.trialverse.model.SemanticIntervention;

/**
 * Created by connor on 3/6/14.
 */
public class InterventionCommand {

  private String name;
  private String motivation;
  private SemanticIntervention semanticIntervention;

  public InterventionCommand() {
  }

  public InterventionCommand(String name, String motivation, SemanticIntervention semanticIntervention) {
    this.name = name;
    this.motivation = motivation;
    this.semanticIntervention = semanticIntervention;
  }

  private void setProjectId(Integer projectId) {
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public SemanticIntervention getSemanticIntervention() {
    return semanticIntervention;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InterventionCommand that = (InterventionCommand) o;

    if (!motivation.equals(that.motivation)) return false;
    if (!name.equals(that.name)) return false;
    if (!semanticIntervention.equals(that.semanticIntervention)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticIntervention.hashCode();
    return result;
  }
}
