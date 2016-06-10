package org.drugis.addis.outcomes.controller.command;

/**
 * Created by connor on 10-6-16.
 */
public class EditOutcomeCommand {

  private String name;
  private String motivation;

  public EditOutcomeCommand() {
  }

  public EditOutcomeCommand(String name, String motivation) {
    this.name = name;
    this.motivation = motivation;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EditOutcomeCommand that = (EditOutcomeCommand) o;

    if (!name.equals(that.name)) return false;
    return motivation != null ? motivation.equals(that.motivation) : that.motivation == null;

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}
