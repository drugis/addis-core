package org.drugis.addis.statistics.model;

import org.drugis.addis.statistics.command.AbstractMeasurementCommand;
import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;

/**
 * Created by joris on 24-1-17.
 */
public class RiskRatio extends AbstractRelativeEffect {
  private final DichotomousMeasurementCommand baseline;
  private final DichotomousMeasurementCommand subject;

  public RiskRatio(DichotomousMeasurementCommand baseline, DichotomousMeasurementCommand subject) {
    super();
    this.baseline = baseline;
    this.subject = subject;
  }

  @Override
  public Distribution getDistribution() {
    return new TransformedLogStudentT(getMu(), getSigma(), getDegreesOfFreedom());
  }

  public boolean isDefined() {
    return baseline.getCount() > 0 && subject.getCount() > 0;
  }

  protected double getMu() {
    if (!isDefined())
      return Double.NaN;

    return Math.log(
            ((double) subject.getCount() / (double) subject.getSampleSize()) /
                    ((double) baseline.getCount() / (double) baseline.getSampleSize()));
  }

  public Double getSigma() { //NB: this is the LOG error
    if (!isDefined())
      return Double.NaN;

    return Math.sqrt((1.0 / (double) subject.getCount()) +
            (1.0 / (double) baseline.getCount()) -
            (1.0 / (double) subject.getSampleSize()) -
            (1.0 / (double) baseline.getSampleSize()));
  }

  protected Integer getDegreesOfFreedom() {
    return getSampleSize() - 2;
  }

  public Integer getSampleSize() {
    return subject.getSampleSize() + baseline.getSampleSize();
  }

  @Override
  public Double getNeutralValue() {
    return 1.0;
  }
}
