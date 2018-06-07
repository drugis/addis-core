package org.drugis.addis.problems.service.model;

/**
 * Created by joris on 14-6-17.
 */
public class RelativePerformanceEntry extends AbstractMeasurementEntry {
  private RelativePerformance performance;

  public RelativePerformanceEntry(String criterion, String dataSource, RelativePerformance performance) {
    super(criterion, dataSource);
    this.performance = performance;
  }

  @Override
  public AbstractPerformance getPerformance() {
    return performance;
  }
}
