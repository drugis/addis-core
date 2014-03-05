package org.drugis.addis.outcomes;

/**
 * Created by daan on 3/5/14.
 */
public class OutcomeCommand {

  private String name;
  private String motivation;
  private String semanticOutcome;

  public OutcomeCommand() {
  }

  public OutcomeCommand(String name, String motivation, String semanticOutcome) {
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcome = semanticOutcome;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public String getSemanticOutcome() {
    return semanticOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OutcomeCommand that = (OutcomeCommand) o;

    if (!motivation.equals(that.motivation)) return false;
    if (!name.equals(that.name)) return false;
    if (!semanticOutcome.equals(that.semanticOutcome)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcome.hashCode();
    return result;
  }
}
