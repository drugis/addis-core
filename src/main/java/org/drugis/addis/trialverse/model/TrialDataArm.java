package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private URI uri;
  private String name;
  private URI studyUri;
  private URI drugInstance;
  private URI drugConcept;
  private Measurement measurement;

  public TrialDataArm() {
  }

  public TrialDataArm(URI uri, String name, URI studyUri, URI drugInstanceUid, URI drugConceptUid, Measurement measurement) {
    this.uri = uri;
    this.name = name;
    this.studyUri = studyUri;
    this.drugInstance = drugInstanceUid;
    this.drugConcept = drugConceptUid;
    this.measurement = measurement;
  }

  public URI getUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public URI getStudyUri() {
    return studyUri;
  }

  public URI getDrugInstanceUid() {
    return drugInstance;
  }

  public URI getDrugConceptUid() {
    return drugConcept;
  }

  public Measurement getMeasurement() {
    return measurement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrialDataArm that = (TrialDataArm) o;

    if (!uri.equals(that.uri)) return false;
    if (!name.equals(that.name)) return false;
    if (studyUri != null ? !studyUri.equals(that.studyUri) : that.studyUri != null) return false;
    if (!drugInstance.equals(that.drugInstance)) return false;
    if (!drugConcept.equals(that.drugConcept)) return false;
    return measurement != null ? measurement.equals(that.measurement) : that.measurement == null;

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (studyUri != null ? studyUri.hashCode() : 0);
    result = 31 * result + drugInstance.hashCode();
    result = 31 * result + drugConcept.hashCode();
    result = 31 * result + (measurement != null ? measurement.hashCode() : 0);
    return result;
  }
}
