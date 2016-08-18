package org.drugis.addis.models.controller.command;

import org.drugis.addis.models.BiasDirection;

/**
 * Created by daan on 18-8-16.
 */
public class CreateFunnelPlotComparisonCommand {

  private Integer t1;
  private Integer t2;
  private BiasDirection biasDirection;

  public CreateFunnelPlotComparisonCommand() {
  }

  public CreateFunnelPlotComparisonCommand(Integer t1, Integer t2, BiasDirection biasDirection) {
    this.t1 = t1;
    this.t2 = t2;
    this.biasDirection = biasDirection;
  }

  public Integer getT1() {
    return t1;
  }

  public Integer getT2() {
    return t2;
  }

  public BiasDirection getBiasDirection() {
    return biasDirection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CreateFunnelPlotComparisonCommand that = (CreateFunnelPlotComparisonCommand) o;

    if (!t1.equals(that.t1)) return false;
    if (!t2.equals(that.t2)) return false;
    return biasDirection == that.biasDirection;

  }

  @Override
  public int hashCode() {
    int result = t1.hashCode();
    result = 31 * result + t2.hashCode();
    result = 31 * result + biasDirection.hashCode();
    return result;
  }
}
