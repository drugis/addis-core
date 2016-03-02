package org.drugis.addis.analyses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
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
    protected Integer modelId;

    public MbrOutcomeInclusionPK() {}

    public MbrOutcomeInclusionPK(Integer metaBenefitRiskAnalysisId, Integer outcomeId, Integer networkMetaAnalysisId, Integer modelId) {
      this.metaBenefitRiskAnalysisId = metaBenefitRiskAnalysisId;
      this.outcomeId = outcomeId;
      this.networkMetaAnalysisId = networkMetaAnalysisId;
      this.modelId = modelId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MbrOutcomeInclusionPK that = (MbrOutcomeInclusionPK) o;

      if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
      if (!outcomeId.equals(that.outcomeId)) return false;
      if (!networkMetaAnalysisId.equals(that.networkMetaAnalysisId)) return false;
      return modelId.equals(that.modelId);

    }

    @Override
    public int hashCode() {
      int result = metaBenefitRiskAnalysisId.hashCode();
      result = 31 * result + outcomeId.hashCode();
      result = 31 * result + networkMetaAnalysisId.hashCode();
      result = 31 * result + modelId.hashCode();
      return result;
    }
  }

  @Id
  private Integer metaBenefitRiskAnalysisId;
  @Id
  private Integer outcomeId;
  @Id
  private Integer networkMetaAnalysisId;
  @Id
  private Integer modelId;

  public MbrOutcomeInclusion() {
  }

  public MbrOutcomeInclusion(Integer metaBenefitRiskAnalysisId, Integer outcomeId, Integer networkMetaAnalysisId, Integer modelId) {
    this.metaBenefitRiskAnalysisId = metaBenefitRiskAnalysisId;
    this.outcomeId = outcomeId;
    this.networkMetaAnalysisId = networkMetaAnalysisId;
    this.modelId = modelId;
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

  public Integer getModelId() {
    return modelId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MbrOutcomeInclusion that = (MbrOutcomeInclusion) o;

    if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    if (!networkMetaAnalysisId.equals(that.networkMetaAnalysisId)) return false;
    return modelId.equals(that.modelId);

  }

  @Override
  public int hashCode() {
    int result = metaBenefitRiskAnalysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + networkMetaAnalysisId.hashCode();
    result = 31 * result + modelId.hashCode();
    return result;
  }
}
