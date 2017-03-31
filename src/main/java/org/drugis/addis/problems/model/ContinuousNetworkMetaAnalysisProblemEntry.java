package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by daan on 21-5-14.
 */
public class ContinuousNetworkMetaAnalysisProblemEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private Double mean;
  private Double stdDev;
  private Integer sampleSize;

  public ContinuousNetworkMetaAnalysisProblemEntry(String study, Integer treatment, Integer sampleSize, Double mean, Double stdDev) {
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

    ContinuousNetworkMetaAnalysisProblemEntry that = (ContinuousNetworkMetaAnalysisProblemEntry) o;

    if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    return sampleSize != null ? sampleSize.equals(that.sampleSize) : that.sampleSize == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    return result;
  }
}
