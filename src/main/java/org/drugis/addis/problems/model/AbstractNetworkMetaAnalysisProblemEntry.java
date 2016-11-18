package org.drugis.addis.problems.model;

/**
 * Created by daan on 21-5-14.
 */
public abstract class AbstractNetworkMetaAnalysisProblemEntry {
  private String study;
  private Integer treatment;

  public AbstractNetworkMetaAnalysisProblemEntry(String study, Integer treatment) {
    this.study = study;
    this.treatment = treatment;
  }

  public String getStudy() {
    return study;
  }

  public Integer getTreatment() {
    return treatment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractNetworkMetaAnalysisProblemEntry that = (AbstractNetworkMetaAnalysisProblemEntry) o;

    if (!study.equals(that.study)) return false;
    return treatment.equals(that.treatment);

  }

  @Override
  public int hashCode() {
    int result = study.hashCode();
    result = 31 * result + treatment.hashCode();
    return result;
  }
}
