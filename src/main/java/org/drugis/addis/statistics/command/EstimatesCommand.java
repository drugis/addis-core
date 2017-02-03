package org.drugis.addis.statistics.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daan on 20-1-17.
 */
public class EstimatesCommand {
  URI baselineUri = null;

  List<AbstractMeasurementCommand> measurements = new ArrayList<>();
  public EstimatesCommand() {
  }

  public EstimatesCommand(List<AbstractMeasurementCommand> measurements) {
    this.measurements = measurements;
  }

  public List<AbstractMeasurementCommand> getMeasurements() {
    return Collections.unmodifiableList(measurements);
  }

  public URI getBaselineUri() {
    return baselineUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EstimatesCommand that = (EstimatesCommand) o;

    if (baselineUri != null ? !baselineUri.equals(that.baselineUri) : that.baselineUri != null) return false;
    if (!measurements.equals(that.measurements)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = baselineUri != null ? baselineUri.hashCode() : 0;
    result = 31 * result + measurements.hashCode();
    return result;
  }
}
