package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private URI uri;
  private String name;
  private URI drugInstance;
  private Measurement measurement;
  private AbstractSemanticIntervention semanticIntervention;

  private Integer matchedProjectInterventionId;


  public TrialDataArm() {
  }

  public TrialDataArm(URI uri, String name, URI drugInstance, Measurement measurement, AbstractSemanticIntervention semanticIntervention) {
    this.uri = uri;
    this.name = name;
    this.drugInstance = drugInstance;
    this.measurement = measurement;
    this.semanticIntervention = semanticIntervention;
  }

  public URI getUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public URI getDrugInstance() {
    return drugInstance;
  }

  public Measurement getMeasurement() {
    return measurement;
  }

  public AbstractSemanticIntervention getSemanticIntervention() {
    return semanticIntervention;
  }

  public Integer getMatchedProjectInterventionId() {
    return matchedProjectInterventionId;
  }

  public void setMatchedProjectInterventionId(Integer matchedProjectInterventionId) {
    this.matchedProjectInterventionId = matchedProjectInterventionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrialDataArm that = (TrialDataArm) o;

    if (!uri.equals(that.uri)) return false;
    if (!name.equals(that.name)) return false;
    if (!drugInstance.equals(that.drugInstance)) return false;
    if (!measurement.equals(that.measurement)) return false;
    if (!semanticIntervention.equals(that.semanticIntervention)) return false;
    return matchedProjectInterventionId != null ? matchedProjectInterventionId.equals(that.matchedProjectInterventionId) : that.matchedProjectInterventionId == null;

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + drugInstance.hashCode();
    result = 31 * result + measurement.hashCode();
    result = 31 * result + semanticIntervention.hashCode();
    result = 31 * result + (matchedProjectInterventionId != null ? matchedProjectInterventionId.hashCode() : 0);
    return result;
  }
}
