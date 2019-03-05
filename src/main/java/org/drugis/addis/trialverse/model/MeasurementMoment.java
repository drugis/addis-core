package org.drugis.addis.trialverse.model;

import java.net.URI;
import java.util.Objects;

/**
 * Created by daan on 11-11-16.
 */
public class MeasurementMoment {
  private final URI uri;
  private final String label;

  public MeasurementMoment(URI uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public URI getUri() {
    return uri;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MeasurementMoment that = (MeasurementMoment) o;
    return Objects.equals(uri, that.uri) &&
            Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {

    return Objects.hash(uri, label);
  }
}
