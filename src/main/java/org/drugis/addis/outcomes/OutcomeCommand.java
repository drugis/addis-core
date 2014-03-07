package org.drugis.addis.outcomes;

import org.drugis.addis.trialverse.model.SemanticOutcome;

/**
 * Created by daan on 3/5/14.
 */
public class OutcomeCommand {

  private Integer projectId;
  private String name;
  private String motivation;
  private SemanticOutcome semanticOutcome;

  public OutcomeCommand() {
  }

  public OutcomeCommand(Integer projectId, String name, String motivation, SemanticOutcome semanticOutcome) {
    this.projectId = projectId;
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcome = semanticOutcome;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public SemanticOutcome getSemanticOutcome() {
    return semanticOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OutcomeCommand that = (OutcomeCommand) o;

    if (!motivation.equals(that.motivation)) return false;
    if (!name.equals(that.name)) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!semanticOutcome.equals(that.semanticOutcome)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcome.hashCode();
    return result;
  }
}
