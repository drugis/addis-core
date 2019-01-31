package org.drugis.addis.analyses.controller;

import org.drugis.addis.analyses.model.AbstractAnalysis;

import java.util.Objects;

/**
 * Created by joris on 3-5-17.
 */
public class AnalysisUpdateCommand {
  private AbstractAnalysis analysis;
  private String scenarioState;

  public AnalysisUpdateCommand() {
  }

  public AnalysisUpdateCommand(AbstractAnalysis analysis, String scenarioState) {
    this.analysis = analysis;
    this.scenarioState = scenarioState;
  }

  public AbstractAnalysis getAnalysis() {
    return analysis;
  }

  public String getScenarioState() {
    return scenarioState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnalysisUpdateCommand that = (AnalysisUpdateCommand) o;
    return Objects.equals(analysis, that.analysis) &&
            Objects.equals(scenarioState, that.scenarioState);
  }

  @Override
  public int hashCode() {

    return Objects.hash(analysis, scenarioState);
  }
}
