package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by joris on 18-11-16.
 */
public class ContinuousStdErrEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private final Double mean;
  private final Double stdErr;

  public ContinuousStdErrEntry(String studyName, Integer treatmentId, Double mean, Double stdErr) {
    super(studyName, treatmentId);
    this.mean = mean;
    this.stdErr = stdErr;
  }

  public Double getMean() {
    return mean;
  }

  @JsonProperty(value = "std.err")
  public Double getStdErr() {
    return stdErr;
  }

  @Override
  public boolean hasMissingValues() {
    return mean == null || stdErr == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousStdErrEntry that = (ContinuousStdErrEntry) o;

    if (!mean.equals(that.mean)) return false;
    return stdErr.equals(that.stdErr);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mean.hashCode();
    result = 31 * result + stdErr.hashCode();
    return result;
  }
}
