package org.drugis.addis.statistics.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daan on 20-1-17.
 */
public class EstimatesCommand {
  List<AbstractMeasurementCommand> measurements = new ArrayList<>();

  public EstimatesCommand() {
  }

  public EstimatesCommand(List<AbstractMeasurementCommand> measurements) {
    this.measurements = measurements;
  }

  public List<AbstractMeasurementCommand> getMeasurements() {
    return Collections.unmodifiableList(measurements);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EstimatesCommand that = (EstimatesCommand) o;

    return measurements.equals(that.measurements);
  }

  @Override
  public int hashCode() {
    return measurements.hashCode();
  }
}
