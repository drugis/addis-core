package org.drugis.addis.subProblems.controller.command;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.util.ObjectToStringDeserializer;

/**
 * Created by joris on 8-5-17.
 */
public class SubProblemCommand {
  private String definition;
  private String title;
  private String scenarioState;

  public String getDefinition() {
    return definition;
  }

  @JsonDeserialize(using = ObjectToStringDeserializer.class)
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getTitle() {
    return title;
  }

  public String getScenarioState() {
    return scenarioState;
  }

  @JsonDeserialize(using = ObjectToStringDeserializer.class)
  public void setScenarioState(String scenarioState) {
    this.scenarioState = scenarioState;
  }

  public SubProblemCommand() {
  }

  public SubProblemCommand(String definition, String title, String scenarioState) {
    this.definition = definition;
    this.title = title;
    this.scenarioState = scenarioState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SubProblemCommand that = (SubProblemCommand) o;

    if (!definition.equals(that.definition)) return false;
    if (!title.equals(that.title)) return false;
    return scenarioState.equals(that.scenarioState);
  }

  @Override
  public int hashCode() {
    int result = definition.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + scenarioState.hashCode();
    return result;
  }
}
