package org.drugis.addis.models.controller.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 18-8-16.
 */
public class CreateFunnelPlotCommand {
  private Integer modelId;
  private List<CreateFunnelPlotComparisonCommand> includedComparisons = new ArrayList<>();

  public CreateFunnelPlotCommand() {
  }

  public CreateFunnelPlotCommand(Integer modelId, List<CreateFunnelPlotComparisonCommand> includedComparisons) {
    this.modelId = modelId;
    this.includedComparisons = includedComparisons;
  }

  public Integer getModelId() {
    return modelId;
  }

  public List<CreateFunnelPlotComparisonCommand> getIncludedComparisons() {
    return includedComparisons;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CreateFunnelPlotCommand that = (CreateFunnelPlotCommand) o;

    if (!modelId.equals(that.modelId)) return false;
    return includedComparisons.equals(that.includedComparisons);

  }

  @Override
  public int hashCode() {
    int result = modelId.hashCode();
    result = 31 * result + includedComparisons.hashCode();
    return result;
  }
}
