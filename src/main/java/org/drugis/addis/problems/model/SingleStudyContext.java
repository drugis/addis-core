package org.drugis.addis.problems.model;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.outcomes.Outcome;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SingleStudyContext {
  private Map<URI, Outcome> outcomesByUri;
  private Map<Integer, AbstractIntervention> interventionsById;
  private Map<URI, String> dataSourceIdsByOutcomeUri;
  private URI sourceLink;
  private Map<Integer, Outcome> outcomesById;

  public SingleStudyContext() {

  }

  public Map<URI, Outcome> getOutcomesByUri() {
    return outcomesByUri;
  }

  public void setOutcomesByUri(Map<URI, Outcome> outcomesByUri) {
    this.outcomesByUri = outcomesByUri;
  }

  public Map<Integer, AbstractIntervention> getInterventionsById() {
    return interventionsById;
  }

  public void setInterventionsById(Map<Integer, AbstractIntervention> interventionsById) {
    this.interventionsById = interventionsById;
  }

  public Map<URI, String> getDataSourceIdsByOutcomeUri() {
    return dataSourceIdsByOutcomeUri;
  }

  public String getDataSourceId(URI outcomeUri){
    return dataSourceIdsByOutcomeUri.get(outcomeUri);
  }

  public void setDataSourceIdsByOutcomeUri(Map<URI, String> dataSourceIdsByOutcomeUri) {
    this.dataSourceIdsByOutcomeUri = dataSourceIdsByOutcomeUri;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  public void setSourceLink(URI sourceLink) {
    this.sourceLink = sourceLink;
  }

  public Map<Integer, Outcome> getOutcomesById() {
    return outcomesById;
  }

  public Outcome getOutcome(Integer id){
    return outcomesById.get(id);
  }

  public void setOutcomesById(Map<Integer, Outcome> outcomesById) {
    this.outcomesById = outcomesById;
  }
}
