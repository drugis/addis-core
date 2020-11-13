package org.drugis.addis.statistics.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EstimatesCommand {
  URI baselineUri;

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
    return Objects.equals(baselineUri, that.baselineUri) &&
            Objects.equals(measurements, that.measurements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baselineUri, measurements);
  }
}
