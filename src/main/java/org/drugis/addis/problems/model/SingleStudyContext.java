package org.drugis.addis.problems.model;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.outcomes.Outcome;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SingleStudyContext {
  private final Map<URI, Outcome> outcomesByUri;
  private final Map<Integer, AbstractIntervention> interventionsById;
  private final Map<URI, String> dataSourceIdsByOutcomeUri;
  private final URI sourceLink;

  public SingleStudyContext(Map<URI,Outcome> outcomesByUri, Map<Integer,AbstractIntervention> interventionsById, Map<URI,String> dataSourceIdsByOutcomeUri, URI sourceLink) {
    this.outcomesByUri = outcomesByUri;
    this.interventionsById = interventionsById;
    this.dataSourceIdsByOutcomeUri = dataSourceIdsByOutcomeUri;
    this.sourceLink = sourceLink;
  }

  public Map<URI, Outcome> getOutcomesByUri() {
    return outcomesByUri;
  }

  public Map<Integer, AbstractIntervention> getInterventionsById() {
    return interventionsById;
  }

  public Map<URI, String> getDataSourceIdsByOutcomeUri() {
    return dataSourceIdsByOutcomeUri;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SingleStudyContext that = (SingleStudyContext) o;
    return Objects.equals(outcomesByUri, that.outcomesByUri) &&
            Objects.equals(interventionsById, that.interventionsById) &&
            Objects.equals(dataSourceIdsByOutcomeUri, that.dataSourceIdsByOutcomeUri) &&
            Objects.equals(sourceLink, that.sourceLink);
  }

  @Override
  public int hashCode() {

    return Objects.hash(outcomesByUri, interventionsById, dataSourceIdsByOutcomeUri, sourceLink);
  }
}
