package org.drugis.addis.problems.model;

/**
 * Created by daan on 21-5-14.
 */
public class ContinuousNetworkMetaAnalysisProblemEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private Double mu;
  private Double sigma;

  public ContinuousNetworkMetaAnalysisProblemEntry(String study, String treatment, Long samplesize, Double mu, Double sigma) {
    super(study, treatment, samplesize);
    this.mu = mu;
    this.sigma = sigma;
  }

  public Double getMu() {
    return mu;
  }

  public Double getSigma() {
    return sigma;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousNetworkMetaAnalysisProblemEntry that = (ContinuousNetworkMetaAnalysisProblemEntry) o;

    if (!mu.equals(that.mu)) return false;
    if (!sigma.equals(that.sigma)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mu.hashCode();
    result = 31 * result + sigma.hashCode();
    return result;
  }
}
