package org.drugis.addis.models.controller.command;

/**
 * Created by connor on 10/27/15.
 */
public class StdDevHeterogeneityPriorCommand extends HeterogeneityPriorCommand {
  private StdDevValuesCommand values;

  public StdDevHeterogeneityPriorCommand() {
  }

  public StdDevHeterogeneityPriorCommand(StdDevValuesCommand values) {
    this.values = values;
  }

  public StdDevValuesCommand getValues() {
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StdDevHeterogeneityPriorCommand that = (StdDevHeterogeneityPriorCommand) o;

    return values != null ? values.equals(that.values) : that.values == null;

  }

  @Override
  public int hashCode() {
    return values != null ? values.hashCode() : 0;
  }
}
