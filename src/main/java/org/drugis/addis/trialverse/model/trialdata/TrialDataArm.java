package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private URI uri;
  private String name;
  private URI drugInstance;
  private List<Measurement> measurements = new ArrayList<>();
  private AbstractSemanticIntervention semanticIntervention;

  private Integer matchedProjectInterventionId;

  public TrialDataArm() {
  }

  public TrialDataArm(URI uri, String name, URI drugInstance, AbstractSemanticIntervention semanticIntervention) {
    this.uri = uri;
    this.name = name;
    this.drugInstance = drugInstance;
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

  public List<Measurement> getMeasurements() {
    return measurements;
  }

  public void addMeasurement(Measurement measurement) {
    this.measurements.add(measurement);
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

    TrialDataArm arm = (TrialDataArm) o;

    if (!uri.equals(arm.uri)) return false;
    if (!name.equals(arm.name)) return false;
    if (!drugInstance.equals(arm.drugInstance)) return false;
    if (!measurements.equals(arm.measurements)) return false;
    if (!semanticIntervention.equals(arm.semanticIntervention)) return false;
    return matchedProjectInterventionId.equals(arm.matchedProjectInterventionId);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + drugInstance.hashCode();
    result = 31 * result + measurements.hashCode();
    result = 31 * result + semanticIntervention.hashCode();
    result = 31 * result + matchedProjectInterventionId.hashCode();
    return result;
  }
}
