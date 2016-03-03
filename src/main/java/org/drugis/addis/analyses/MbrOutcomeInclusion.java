package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.util.ObjectToStringDeserializer;

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
  @JsonRawValue
  private String baseline;

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

  @JsonRawValue
  public String getBaseline() {
    return baseline;
  }

  @JsonDeserialize(using = ObjectToStringDeserializer.class)
  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MbrOutcomeInclusion that = (MbrOutcomeInclusion) o;

    if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    if (!networkMetaAnalysisId.equals(that.networkMetaAnalysisId)) return false;
    if (!modelId.equals(that.modelId)) return false;
    return baseline != null ? baseline.equals(that.baseline) : that.baseline == null;

  }

  @Override
  public int hashCode() {
    int result = metaBenefitRiskAnalysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + networkMetaAnalysisId.hashCode();
    result = 31 * result + modelId.hashCode();
    result = 31 * result + (baseline != null ? baseline.hashCode() : 0);
    return result;
  }
}
