package org.drugis.addis.models.controller.command;

/**
 * Created by connor on 10/27/15.
 */
public class VarianceHeterogeneityPriorCommand extends HeterogeneityPriorCommand {
  private VarianceValuesCommand values;

  public VarianceHeterogeneityPriorCommand() {
  }

  public VarianceHeterogeneityPriorCommand(VarianceValuesCommand values) {
    this.values = values;
  }

  public VarianceValuesCommand getValues() {
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VarianceHeterogeneityPriorCommand that = (VarianceHeterogeneityPriorCommand) o;

    return values.equals(that.values);

  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
