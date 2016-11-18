package org.drugis.addis.problems.model;

/**
 * Created by daan on 21-5-14.
 */
public class RateNetworkMetaAnalysisProblemEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private Integer responders;
  private Integer sampleSize;

  public RateNetworkMetaAnalysisProblemEntry(String study, Integer treatment, Integer samplesize, Integer responders) {
    super(study, treatment);
    this.sampleSize = samplesize;
    this.responders = responders;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public Integer getResponders() {
    return responders;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    RateNetworkMetaAnalysisProblemEntry that = (RateNetworkMetaAnalysisProblemEntry) o;

    if (responders != null ? !responders.equals(that.responders) : that.responders != null) return false;
    return sampleSize != null ? sampleSize.equals(that.sampleSize) : that.sampleSize == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (responders != null ? responders.hashCode() : 0);
    result = 31 * result + (sampleSize != null ? sampleSize.hashCode() : 0);
    return result;
  }
}
