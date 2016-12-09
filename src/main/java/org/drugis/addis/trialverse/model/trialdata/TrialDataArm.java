package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.*;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private URI uri;
  private String name;
  private Map<URI, Set<Measurement>> measurements = new HashMap<>();
  private List<AbstractSemanticIntervention> semanticInterventions = new ArrayList<>();

  private Set<Integer> matchedProjectInterventionIds = new HashSet<>();

  public TrialDataArm() {
  }

  public TrialDataArm(URI uri, String name) {
    this.uri = uri;
    this.name = name;
  }

  public URI getUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public Map<URI, Set<Measurement>> getMeasurements() {
    return measurements;
  }

  public Set<Measurement> getMeasurementsForMoment(URI measurementMomentUri) {
    return measurements.get(measurementMomentUri);
  }

  public void addMeasurement(URI measurementMomentUri, Measurement measurement) {
    if(measurements.get(measurementMomentUri) == null){
      this.measurements.put(measurementMomentUri,new HashSet<>());
    }
    this.measurements.get(measurementMomentUri).add(measurement);
  }

  public void addSemanticIntervention(AbstractSemanticIntervention abstractSemanticIntervention) {
    this.semanticInterventions.add(abstractSemanticIntervention);
  }

  public List<AbstractSemanticIntervention> getSemanticInterventions() {
    return semanticInterventions;
  }

  public Set<Integer> getMatchedProjectInterventionIds() {
    return matchedProjectInterventionIds;
  }

  public void setMatchedProjectInterventionIds(Set<Integer> matchedProjectInterventionId) {
    this.matchedProjectInterventionIds = matchedProjectInterventionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrialDataArm arm = (TrialDataArm) o;

    if (!uri.equals(arm.uri)) return false;
    if (!name.equals(arm.name)) return false;
    if (!measurements.equals(arm.measurements)) return false;
    if (!semanticInterventions.equals(arm.semanticInterventions)) return false;
    return matchedProjectInterventionIds.equals(arm.matchedProjectInterventionIds);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + measurements.hashCode();
    result = 31 * result + semanticInterventions.hashCode();
    result = 31 * result + matchedProjectInterventionIds.hashCode();
    return result;
  }
}
