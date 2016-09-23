package org.drugis.addis.outcomes.controller.command;

/**
 * Created by connor on 10-6-16.
 */
public class EditOutcomeCommand {

  private final static int HIGHER_IS_BETTER = 1;
  private final static int LOWER_IS_BETTER = -1;

  private Integer direction = HIGHER_IS_BETTER; // higher is better is the default
  private String name;
  private String motivation;

  public EditOutcomeCommand() {
  }

  public EditOutcomeCommand(String name, String motivation, Integer direction) throws Exception {
    this.name = name;
    this.motivation = motivation;
    if(direction.intValue() != HIGHER_IS_BETTER && direction.intValue() != LOWER_IS_BETTER) {
      throw new Exception("invalid direction value, must be 1 either or -1");
    }
    this.direction = direction;
  }

  public Integer getDirection() {
    return direction;
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

    if (!direction.equals(that.direction)) return false;
    if (!name.equals(that.name)) return false;
    return motivation != null ? motivation.equals(that.motivation) : that.motivation == null;

  }

  @Override
  public int hashCode() {
    int result = direction.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}
