package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by daan on 11-11-16.
 */
public class MeasurementMoment {
  private final URI measurementMoment;
  private final String label;

  public MeasurementMoment(URI measurementMoment, String label) {
    this.measurementMoment = measurementMoment;
    this.label = label;
  }

  public URI getMeasurementMoment() {
    return measurementMoment;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MeasurementMoment that = (MeasurementMoment) o;

    if (!measurementMoment.equals(that.measurementMoment)) return false;
    return label.equals(that.label);

  }

  @Override
  public int hashCode() {
    int result = measurementMoment.hashCode();
    result = 31 * result + label.hashCode();
    return result;
  }
}
