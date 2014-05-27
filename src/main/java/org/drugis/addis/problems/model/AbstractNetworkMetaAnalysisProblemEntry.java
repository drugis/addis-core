package org.drugis.addis.problems.model;

/**
 * Created by daan on 21-5-14.
 */
public abstract class AbstractNetworkMetaAnalysisProblemEntry {
    private String study;
    private String treatment;
  private Long sampleSize;

  public AbstractNetworkMetaAnalysisProblemEntry(String study, String treatment, Long sampleSize) {
    this.study = study;
    this.treatment = treatment;
    this.sampleSize = sampleSize;
  }

  public String getStudy() {
    return study;
  }

  public String getTreatment() {
    return treatment;
  }

  public Long getSampleSize() {
    return sampleSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractNetworkMetaAnalysisProblemEntry that = (AbstractNetworkMetaAnalysisProblemEntry) o;

    if (!sampleSize.equals(that.sampleSize)) return false;
    if (!study.equals(that.study)) return false;
    if (!treatment.equals(that.treatment)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = study.hashCode();
    result = 31 * result + treatment.hashCode();
    result = 31 * result + sampleSize.hashCode();
    return result;
  }
}
