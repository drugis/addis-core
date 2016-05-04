package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by daan on 21-5-14.
 */
public class ContinuousNetworkMetaAnalysisProblemEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private Double mean;
  private Double stdDev;

  public ContinuousNetworkMetaAnalysisProblemEntry(String study, Integer treatment, Integer samplesize, Double mean, Double stdDev) {
    super(study, treatment, samplesize);
    this.mean = mean;
    this.stdDev = stdDev;
  }

  public Double getMean() {
    return mean;
  }

  @JsonProperty(value = "std.dev")
  public Double getStdDev() {
    return stdDev;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousNetworkMetaAnalysisProblemEntry that = (ContinuousNetworkMetaAnalysisProblemEntry) o;

    if (!mean.equals(that.mean)) return false;
    if (!stdDev.equals(that.stdDev)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mean.hashCode();
    result = 31 * result + stdDev.hashCode();
    return result;
  }
}
