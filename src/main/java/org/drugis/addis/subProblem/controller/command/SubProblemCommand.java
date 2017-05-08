package org.drugis.addis.subProblem.controller.command;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Created by joris on 8-5-17.
 */
public class SubProblemCommand {
  private String definition;
  private String title;

  public String getDefinition() {
    return definition;
  }

  public String getTitle() {
    return title;
  }

  public SubProblemCommand() {
  }

  public SubProblemCommand(String definition, String title) {
    this.definition = definition;
    this.title = title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SubProblemCommand that = (SubProblemCommand) o;

    return definition.equals(that.definition) && title.equals(that.title);
  }

  @Override
  public int hashCode() {
    int result = definition.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }
}
