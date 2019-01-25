package org.drugis.addis.problems.model;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.outcomes.Outcome;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class SingleStudyContext {
  private Map<Integer, AbstractIntervention> interventionsById;
  private URI sourceLink;
  private Outcome outcome;
  private String dataSourceUuid;
  private Outcome outcome;

  public SingleStudyContext() {

  }

  public Outcome getOutcome() {
    return outcome;
  }

  public void setOutcome(Outcome outcome) {
    this.outcome = outcome;
  }

  public String dataSourceUuid() {
    return dataSourceUuid;
  }

  public void setDataSourceUuid(String dataSourceUri) {
    this.dataSourceUuid = dataSourceUri;
  }

  public String getDataSourceUuid() {
    return dataSourceUuid;
  }

  public Map<Integer, AbstractIntervention> getInterventionsById() {
    return interventionsById;
  }

  public void setInterventionsById(Map<Integer, AbstractIntervention> interventionsById) {
    this.interventionsById = interventionsById;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  public void setSourceLink(URI sourceLink) {
    this.sourceLink = sourceLink;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SingleStudyContext that = (SingleStudyContext) o;
    return Objects.equals(interventionsById, that.interventionsById) &&
            Objects.equals(sourceLink, that.sourceLink) &&
            Objects.equals(outcome, that.outcome) &&
            Objects.equals(dataSourceUuid, that.dataSourceUuid) &&
            Objects.equals(outcome, that.outcome);
  }

  @Override
  public int hashCode() {

    return Objects.hash(interventionsById, sourceLink, outcome, dataSourceUuid, outcome);
  }
}
