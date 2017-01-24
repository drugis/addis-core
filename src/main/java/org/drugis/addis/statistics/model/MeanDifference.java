package org.drugis.addis.statistics.model;

import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;

/**
 * Created by joris on 24-1-17.
 */
public class MeanDifference extends AbstractRelativeEffect {
  private final ContinuousMeasurementCommand baseline;
  private final ContinuousMeasurementCommand subject;

  public MeanDifference(ContinuousMeasurementCommand baseline, ContinuousMeasurementCommand subject) {
    super();
    this.baseline = baseline;
    this.subject = subject;
  }

  public ContinuousMeasurementCommand getBaseline() {
    return baseline;
  }

  public ContinuousMeasurementCommand getSubject() {
    return subject;
  }

  @Override
  public Distribution getDistribution() {
    return new TransformedStudentT(getMu(), getSigma(), getDegreesOfFreedom());
  }

  @Override
  public Double getNeutralValue() {
    return 0.0;
  }

  public Double getMu() {
    return getCorrectionJ() * getCohenD();
  }

  private Double getCorrectionJ() {
    return (1 - (3 / (4 * (double) getDegreesOfFreedom() - 1)));
  }

  private Double getCohenD() {
    return (subject.getMean() - baseline.getMean()) / getPooledStdDev();
  }

  private Double getPooledStdDev() {
    Double numerator = (subject.getSampleSize() - 1) * square(subject.getStdDev())
            + (baseline.getSampleSize() - 1) * square(baseline.getStdDev());
    return Math.sqrt(numerator / getDegreesOfFreedom());
  }

  private Double square(Double x) {
    return x * x;
  }

  public Double getSigma() {
    return Math.sqrt(square(getCorrectionJ()) * getCohenVariance());
  }

  private Double getCohenVariance() {
    Double fraction1 = (double) (getSampleSize() / (subject.getSampleSize() * baseline.getSampleSize()));
    Double fraction2 = square(getCohenD()) / (2.0 *  getSampleSize());
    return fraction1 + fraction2;
  }

  public Integer getDegreesOfFreedom() {
    return getSampleSize() - 2;
  }

  public Integer getSampleSize() {
    return subject.getSampleSize() + baseline.getSampleSize();
  }
}
