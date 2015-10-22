package org.drugis.addis.models;

/**
 * Created by daan on 22-10-15.
 */
public class HeterogeneityPriorCommand {
  private String type;
  private HeterogeneityValuesCommand heterogeneityValuesCommand;

  public HeterogeneityPriorCommand() {
  }

  public HeterogeneityPriorCommand(String type, HeterogeneityValuesCommand heterogeneityValuesCommand) {
    this.type = type;
    this.heterogeneityValuesCommand = heterogeneityValuesCommand;
  }

  public String getType() {
    return type;
  }

  public HeterogeneityValuesCommand getHeterogeneityValuesCommand() {
    return heterogeneityValuesCommand;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HeterogeneityPriorCommand that = (HeterogeneityPriorCommand) o;

    if (!type.equals(that.type)) return false;
    return !(heterogeneityValuesCommand != null ? !heterogeneityValuesCommand.equals(that.heterogeneityValuesCommand) : that.heterogeneityValuesCommand != null);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (heterogeneityValuesCommand != null ? heterogeneityValuesCommand.hashCode() : 0);
    return result;
  }
}
