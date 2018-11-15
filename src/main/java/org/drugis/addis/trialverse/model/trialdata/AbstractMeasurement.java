package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.Objects;

public class  AbstractMeasurement {
  private URI studyUuid;
  private URI variableUri;
  private URI variableConceptUri;
  private URI armUri;
  private URI measurementTypeURI;

  public AbstractMeasurement() {
  }

  public AbstractMeasurement(URI studyUuid, URI variableUri, URI variableConceptUri, URI armUri, URI measurementTypeURI) {
    this.studyUuid = studyUuid;
    this.variableUri = variableUri;
    this.variableConceptUri = variableConceptUri;
    this.armUri = armUri;
    this.measurementTypeURI = measurementTypeURI;
  }

  public URI getStudyUuid() {
    return studyUuid;
  }

  public URI getVariableUri() {
    return variableUri;
  }

  public URI getVariableConceptUri() {
    return variableConceptUri;
  }

  public URI getArmUri() {
    return armUri;
  }

  public URI getMeasurementTypeURI() {
    return measurementTypeURI;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractMeasurement that = (AbstractMeasurement) o;
    return Objects.equals(studyUuid, that.studyUuid) &&
            Objects.equals(variableUri, that.variableUri) &&
            Objects.equals(variableConceptUri, that.variableConceptUri) &&
            Objects.equals(armUri, that.armUri) &&
            Objects.equals(measurementTypeURI, that.measurementTypeURI);
  }

  @Override
  public int hashCode() {

    return Objects.hash(studyUuid, variableUri, variableConceptUri, armUri, measurementTypeURI);
  }
}
