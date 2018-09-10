package org.drugis.addis.outcomes;

import org.apache.commons.lang3.StringUtils;
import org.drugis.addis.trialverse.model.SemanticVariable;

/**
 * Created by daan on 3/5/14.
 */
public class OutcomeCommand {

  private final static int HIGHER_IS_BETTER = 1;
  private final static int LOWER_IS_BETTER = -1;

  private Integer projectId;
  private String name;
  private Integer direction = HIGHER_IS_BETTER; // higher is better is the default
  private String motivation;
  private SemanticVariable semanticOutcome;

  public OutcomeCommand() {
  }

  public OutcomeCommand(Integer projectId, String name, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this(projectId, name, HIGHER_IS_BETTER, motivation, semanticOutcome);
  }

  public OutcomeCommand(Integer projectId, String name, Integer direction, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this.projectId = projectId;
    this.name = name;
    if(direction.intValue() != HIGHER_IS_BETTER && direction.intValue() != LOWER_IS_BETTER) {
      throw new Exception("invalid direction value, must be 1 either or -1");
    }
    this.direction = direction;
    this.motivation = motivation;
    this.semanticOutcome = semanticOutcome;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public Integer getDirection() {
    return direction;
  }

  public String getMotivation() {
    return motivation == null ? StringUtils.EMPTY : motivation;
  }

  public SemanticVariable getSemanticOutcome() {
    return semanticOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OutcomeCommand that = (OutcomeCommand) o;

    if (!projectId.equals(that.projectId)) return false;
    if (!name.equals(that.name)) return false;
    if (!direction.equals(that.direction)) return false;
    if (motivation != null ? !motivation.equals(that.motivation) : that.motivation != null) return false;
    return semanticOutcome != null ? semanticOutcome.equals(that.semanticOutcome) : that.semanticOutcome == null;

  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + direction.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + (semanticOutcome != null ? semanticOutcome.hashCode() : 0);
    return result;
  }
}
