package org.drugis.addis.problems.model;

/**
 * Created by daan on 21-5-14.
 */
public class RateNetworkMetaAnalysisProblemEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private Integer responders;

  public RateNetworkMetaAnalysisProblemEntry(String study, Integer treatment, Integer samplesize, Integer responders) {
    super(study, treatment, samplesize);
    this.responders = responders;
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

    if (!responders.equals(that.responders)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + responders.hashCode();
    return result;
  }
}
