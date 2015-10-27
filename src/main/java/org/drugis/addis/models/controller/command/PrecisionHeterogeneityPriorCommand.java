package org.drugis.addis.models.controller.command;

/**
 * Created by connor on 10/27/15.
 */
public class PrecisionHeterogeneityPriorCommand extends HeterogeneityPriorCommand {
  private PrecisionValuesCommand values;

  public PrecisionHeterogeneityPriorCommand() {
  }

  public PrecisionHeterogeneityPriorCommand(PrecisionValuesCommand values) {
    this.values = values;
  }

  public PrecisionValuesCommand getValues() {
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PrecisionHeterogeneityPriorCommand that = (PrecisionHeterogeneityPriorCommand) o;

    return values.equals(that.values);

  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
