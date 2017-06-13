package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.util.ObjectToStringDeserializer;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by connor on 26-2-16.
 */
@Entity
@IdClass(BenefitRiskNMAOutcomeInclusion.BrOutcomeInclusionPK.class)
public class BenefitRiskNMAOutcomeInclusion {

  public static class BrOutcomeInclusionPK  {
    protected Integer analysisId;
    protected Integer outcomeId;
    protected Integer networkMetaAnalysisId;
    protected Integer modelId;

    public BrOutcomeInclusionPK() {}

    public BrOutcomeInclusionPK(Integer analysisId, Integer outcomeId, Integer networkMetaAnalysisId, Integer modelId) {
      this.analysisId = analysisId;
      this.outcomeId = outcomeId;
      this.networkMetaAnalysisId = networkMetaAnalysisId;
      this.modelId = modelId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      BrOutcomeInclusionPK that = (BrOutcomeInclusionPK) o;

      if (!analysisId.equals(that.analysisId)) return false;
      if (!outcomeId.equals(that.outcomeId)) return false;
      if (networkMetaAnalysisId != null ? !networkMetaAnalysisId.equals(that.networkMetaAnalysisId) : that.networkMetaAnalysisId != null)
        return false;
      return modelId != null ? modelId.equals(that.modelId) : that.modelId == null;
    }

    @Override
    public int hashCode() {
      int result = analysisId.hashCode();
      result = 31 * result + outcomeId.hashCode();
      result = 31 * result + (networkMetaAnalysisId != null ? networkMetaAnalysisId.hashCode() : 0);
      result = 31 * result + (modelId != null ? modelId.hashCode() : 0);
      return result;
    }
  }

  @Id
  private Integer analysisId;
  @Id
  private Integer outcomeId;
  @Id
  private Integer networkMetaAnalysisId;
  @Id
  private Integer modelId;

  @JsonRawValue
  private String baseline;

  public BenefitRiskNMAOutcomeInclusion() {
  }

  public BenefitRiskNMAOutcomeInclusion(Integer analysisId, Integer outcomeId, Integer networkMetaAnalysisId, Integer modelId) {
    this.analysisId = analysisId;
    this.outcomeId = outcomeId;
    this.networkMetaAnalysisId = networkMetaAnalysisId;
    this.modelId = modelId;
  }

  public Integer getAnalysisId() {
    return analysisId;
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

    BenefitRiskNMAOutcomeInclusion that = (BenefitRiskNMAOutcomeInclusion) o;

    if (!analysisId.equals(that.analysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    if (networkMetaAnalysisId != null ? !networkMetaAnalysisId.equals(that.networkMetaAnalysisId) : that.networkMetaAnalysisId != null)
      return false;
    if (modelId != null ? !modelId.equals(that.modelId) : that.modelId != null) return false;
    return baseline != null ? baseline.equals(that.baseline) : that.baseline == null;
  }

  @Override
  public int hashCode() {
    int result = analysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + (networkMetaAnalysisId != null ? networkMetaAnalysisId.hashCode() : 0);
    result = 31 * result + (modelId != null ? modelId.hashCode() : 0);
    result = 31 * result + (baseline != null ? baseline.hashCode() : 0);
    return result;
  }
}
