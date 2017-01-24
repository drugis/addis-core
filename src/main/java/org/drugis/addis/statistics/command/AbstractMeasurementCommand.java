package org.drugis.addis.statistics.command;

import java.net.URI;

/**
 * Created by daan on 20-1-17.
 */
public abstract class AbstractMeasurementCommand {
  URI endpointUri;
  URI armUri;

  public AbstractMeasurementCommand() {
  }

  public AbstractMeasurementCommand(URI endpointUri, URI armUri) {
    this.endpointUri = endpointUri;
    this.armUri = armUri;
  }

  public URI getEndpointUri() {
    return endpointUri;
  }

  public URI getArmUri() {
    return armUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractMeasurementCommand that = (AbstractMeasurementCommand) o;

    if (!endpointUri.equals(that.endpointUri)) return false;
    return armUri.equals(that.armUri);
  }

  @Override
  public int hashCode() {
    int result = endpointUri.hashCode();
    result = 31 * result + armUri.hashCode();
    return result;
  }
}
