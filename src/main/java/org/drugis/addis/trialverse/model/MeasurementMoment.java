package org.drugis.addis.trialverse.model;

import java.net.URI;

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

    if (!uri.equals(that.uri)) return false;
    return label.equals(that.label);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + label.hashCode();
    return result;
  }
}
