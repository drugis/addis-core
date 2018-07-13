package org.drugis.addis.problems.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;

import java.util.Set;

public class NMAInclusionWithResults {
  private final Outcome outcome;
  private final Model model;
  private final JsonNode pataviResults;
  private Set<AbstractIntervention> interventions;
  private String baseline;

  public NMAInclusionWithResults(Outcome outcome, Model model, JsonNode pataviResults, Set<AbstractIntervention> interventions, String baseline) {

    this.outcome = outcome;
    this.model = model;
    this.pataviResults = pataviResults;
    this.interventions = interventions;
    this.baseline = baseline;
  }

  public Outcome getOutcome() {
    return outcome;
  }

  public Model getModel() {
    return model;
  }

  public JsonNode getPataviResults() {
    return pataviResults;
  }

  public Set<AbstractIntervention> getInterventions() {
    return interventions;
  }

  public String getBaseline() {
    return baseline;
  }
}
