package org.drugis.addis.statistics.model;

import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;
import org.drugis.addis.statistics.exception.MissingMeasurementException;

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
  public Distribution getDistribution() throws MissingMeasurementException {
    return new TransformedStudentT(getMu(), getSigma(), getDegreesOfFreedom());
  }

  @Override
  public Double getNeutralValue() {
    return 0.0;
  }

  public Double getMu() throws MissingMeasurementException {
    return subject.getMean() - baseline.getMean();
  }

  private Double square(Double x) {
    return x * x;
  }

  public Double getSigma() throws MissingMeasurementException {
    return Math.sqrt(square(subject.getStdDev()) / subject.getSampleSize()
            + square(baseline.getStdDev()) / baseline.getSampleSize());
  }


  public Integer getDegreesOfFreedom() throws MissingMeasurementException {
    return getSampleSize() - 2;
  }

  public Integer getSampleSize() throws MissingMeasurementException {
    return subject.getSampleSize() + baseline.getSampleSize();
  }
}
