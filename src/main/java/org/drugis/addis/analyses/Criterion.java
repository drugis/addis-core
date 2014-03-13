package org.drugis.addis.analyses;

/**
 * Created by connor on 3/13/14.
 */
public class Criterion {

  private Integer analysisId;
  private Integer outcomeId;

  public Criterion() {
  }

  public Criterion(Integer analysisId, Integer outcomeId) {
    this.analysisId = analysisId;
    this.outcomeId = outcomeId;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getOutcomeId() {
    return outcomeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Criterion)) return false;

    Criterion criterion = (Criterion) o;

    if (!analysisId.equals(criterion.analysisId)) return false;
    if (!outcomeId.equals(criterion.outcomeId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = analysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    return result;
  }
}
