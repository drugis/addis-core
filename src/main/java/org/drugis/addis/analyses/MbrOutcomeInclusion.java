package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by connor on 26-2-16.
 */
@Entity
public class MbrOutcomeInclusion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;

  private MetaBenefitRiskAnalysis metaBenefitRiskAnalysisId;

  private Integer outcomeId;

  private Integer networkMetaAnalysisId;

  public MbrOutcomeInclusion() {
  }

  public MbrOutcomeInclusion(MetaBenefitRiskAnalysis metaBenefitRiskAnalysisId, Integer outcomeId, Integer networkMetaAnalysisId) {
    this.metaBenefitRiskAnalysisId = metaBenefitRiskAnalysisId;
    this.outcomeId = outcomeId;
    this.networkMetaAnalysisId = networkMetaAnalysisId;
  }

  public Integer getId() {
    return id;
  }

  public MetaBenefitRiskAnalysis getMetaBenefitRiskAnalysisId() {
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
    if (o == null || getClass() != o.getClass()) return false;

    MbrOutcomeInclusion that = (MbrOutcomeInclusion) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!metaBenefitRiskAnalysisId.equals(that.metaBenefitRiskAnalysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    return networkMetaAnalysisId.equals(that.networkMetaAnalysisId);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + metaBenefitRiskAnalysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + networkMetaAnalysisId.hashCode();
    return result;
  }
}
