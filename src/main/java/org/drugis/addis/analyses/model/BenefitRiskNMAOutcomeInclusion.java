package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.util.ObjectToStringDeserializer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@IdClass(BenefitRiskNMAOutcomeInclusion.BrOutcomeInclusionPK.class)
public class BenefitRiskNMAOutcomeInclusion {

  public static class BrOutcomeInclusionPK implements Serializable {
    protected Integer analysisId;
    protected Integer outcomeId;
    protected Integer networkMetaAnalysisId;
    protected Integer modelId;

    public BrOutcomeInclusionPK() {
    }

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
      return Objects.equals(analysisId, that.analysisId) &&
              Objects.equals(outcomeId, that.outcomeId) &&
              Objects.equals(networkMetaAnalysisId, that.networkMetaAnalysisId) &&
              Objects.equals(modelId, that.modelId);
    }

    @Override
    public int hashCode() {

      return Objects.hash(analysisId, outcomeId, networkMetaAnalysisId, modelId);
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

  public void setBaselineThroughString(String baseline){
    this.baseline = baseline;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BenefitRiskNMAOutcomeInclusion that = (BenefitRiskNMAOutcomeInclusion) o;
    return Objects.equals(analysisId, that.analysisId) &&
            Objects.equals(outcomeId, that.outcomeId) &&
            Objects.equals(networkMetaAnalysisId, that.networkMetaAnalysisId) &&
            Objects.equals(modelId, that.modelId) &&
            Objects.equals(baseline, that.baseline);
  }

  @Override
  public int hashCode() {

    return Objects.hash(analysisId, outcomeId, networkMetaAnalysisId, modelId, baseline);
  }
}
