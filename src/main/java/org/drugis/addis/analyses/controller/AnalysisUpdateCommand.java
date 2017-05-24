package org.drugis.addis.analyses.controller;

import org.drugis.addis.analyses.AbstractAnalysis;

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

    if (!analysis.equals(that.analysis)) return false;
    return scenarioState != null ? scenarioState.equals(that.scenarioState) : that.scenarioState == null;
  }

  @Override
  public int hashCode() {
    int result = analysis.hashCode();
    result = 31 * result + (scenarioState != null ? scenarioState.hashCode() : 0);
    return result;
  }
}
