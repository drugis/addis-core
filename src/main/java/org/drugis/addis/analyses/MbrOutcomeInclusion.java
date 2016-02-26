package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by connor on 26-2-16.
 */
@Entity
@IdClass(MbrOutcomeInclusion.MbrOutcomeInclusionPK.class)
public class MbrOutcomeInclusion implements Serializable {

  public static class MbrOutcomeInclusionPK implements Serializable {
    protected Integer metaBenefitRiskAnalysisId;
    protected Integer outcomeId;
    protected Integer networkMetaAnalysisId;

    public MbrOutcomeInclusionPK() {}

    public MbrOutcomeInclusionPK(Integer metaBenefitRiskAnalysisId, Integer outcomeId, Integer networkMetaAnalysisId) {
      this.metaBenefitRiskAnalysisId = metaBenefitRiskAnalysisId;
      this.outcomeId = outcomeId;
      this.networkMetaAnalysisId = networkMetaAnalysisId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MbrOutcomeInclusionPK)) return false;

      MbrOutcomeInclusionPK that = (MbrOutcomeInclusionPK) o;

      if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
      if (!outcomeId.equals(that.outcomeId)) return false;
      return networkMetaAnalysisId.equals(that.networkMetaAnalysisId);

    }

    @Override
    public int hashCode() {
      int result = metaBenefitRiskAnalysisId.hashCode();
      result = 31 * result + outcomeId.hashCode();
      result = 31 * result + networkMetaAnalysisId.hashCode();
      return result;
    }
  }

  @Id
  private Integer metaBenefitRiskAnalysisId;
  @Id
  private Integer outcomeId;
  @Id
  private Integer networkMetaAnalysisId;

  public MbrOutcomeInclusion() {
  }

  public MbrOutcomeInclusion(Integer metaBenefitRiskAnalysisId, Integer outcomeId, Integer networkMetaAnalysisId) {
    this.metaBenefitRiskAnalysisId = metaBenefitRiskAnalysisId;
    this.outcomeId = outcomeId;
    this.networkMetaAnalysisId = networkMetaAnalysisId;
  }

  public Integer getMetaBenefitRiskAnalysisId() {
    return metaBenefitRiskAnalysisId;
  }

  public Integer getOutcomeId() {
    return outcomeId;
  }

  public Integer getNetworkMetaAnalysisId() {
    return networkMetaAnalysisId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MbrOutcomeInclusion)) return false;

    MbrOutcomeInclusion that = (MbrOutcomeInclusion) o;

    if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    return networkMetaAnalysisId.equals(that.networkMetaAnalysisId);

  }

  @Override
  public int hashCode() {
    int result = metaBenefitRiskAnalysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + networkMetaAnalysisId.hashCode();
    return result;
  }
}
