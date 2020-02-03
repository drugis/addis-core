package org.drugis.addis.problems.model;

import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
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
  private BenefitRiskStudyOutcomeInclusion inclusion;
  private String source;

  public SingleStudyContext() {

  }

  public Outcome getOutcome() {
    return outcome;
  }

  public void setOutcome(Outcome outcome) {
    this.outcome = outcome;
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

  public BenefitRiskStudyOutcomeInclusion getInclusion() {
    return inclusion;
  }

  public void setInclusion(BenefitRiskStudyOutcomeInclusion inclusion) {
    this.inclusion = inclusion;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSource() {
    return this.source;
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
            Objects.equals(inclusion, that.inclusion) &&
            Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interventionsById, sourceLink, outcome, dataSourceUuid, inclusion, source);
  }
}
