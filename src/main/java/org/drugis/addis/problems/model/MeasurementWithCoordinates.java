package org.drugis.addis.problems.model;

import org.drugis.addis.trialverse.model.trialdata.Measurement;

import java.net.URI;
import java.util.Objects;

public class MeasurementWithCoordinates {
  private final Measurement measurement;
  private final Integer interventionId;
  private final String dataSource;
  private final URI conceptOutcome;

  public MeasurementWithCoordinates(Measurement measurement, Integer interventionId, String dataSource) {
    this.measurement = measurement;
    this.interventionId = interventionId;
    this.dataSource = dataSource;
    this.conceptOutcome = null;
  }

  public MeasurementWithCoordinates(Measurement measurement, Integer interventionId, String dataSource, URI conceptOutcome) {
    this.measurement = measurement;
    this.interventionId = interventionId;
    this.dataSource = dataSource;
    this.conceptOutcome = conceptOutcome;
  }

  public Measurement getMeasurement() {
    return measurement;
  }

  public Integer getInterventionId() {
    return interventionId;
  }

  public String getDataSource() {
    return dataSource;
  }

  public URI getConceptOutcome() {
    return conceptOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MeasurementWithCoordinates that = (MeasurementWithCoordinates) o;
    return Objects.equals(measurement, that.measurement) &&
            Objects.equals(interventionId, that.interventionId) &&
            Objects.equals(dataSource, that.dataSource) &&
            Objects.equals(conceptOutcome, that.conceptOutcome);
  }

  @Override
  public int hashCode() {

    return Objects.hash(measurement, interventionId, dataSource, conceptOutcome);
  }
}
