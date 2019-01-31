package org.drugis.addis.problems.model.problemEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by daan on 21-5-14.
 */
public class AbsoluteContinuousProblemEntry extends AbstractProblemEntry {
  private Double mean;
  private Double stdDev;
  private Integer sampleSize;

  public AbsoluteContinuousProblemEntry(String study, Integer treatment, Integer sampleSize, Double mean, Double stdDev) {
    super(study, treatment);
    this.mean = mean;
    this.stdDev = stdDev;
    this.sampleSize = sampleSize;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public Double getMean() {
    return mean;
  }

  @JsonProperty(value = "std.dev")
  public Double getStdDev() {
    return stdDev;
  }

  @Override
  public boolean hasMissingValues() {
    return mean == null || stdDev == null || sampleSize == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    AbsoluteContinuousProblemEntry that = (AbsoluteContinuousProblemEntry) o;
    return Objects.equals(mean, that.mean) &&
            Objects.equals(stdDev, that.stdDev) &&
            Objects.equals(sampleSize, that.sampleSize);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), mean, stdDev, sampleSize);
  }
}
