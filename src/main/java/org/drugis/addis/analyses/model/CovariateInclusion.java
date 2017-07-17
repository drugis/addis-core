package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by connor on 18-6-14.
 */
@Entity
public class CovariateInclusion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;

  private Integer analysisId;

  private Integer covariateId;

  public CovariateInclusion() {
  }

  public CovariateInclusion(Integer analysisId, Integer covariateId) {
    this.analysisId = analysisId;
    this.covariateId = covariateId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getCovariateId() {
    return covariateId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CovariateInclusion)) return false;

    CovariateInclusion that = (CovariateInclusion) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!analysisId.equals(that.analysisId)) return false;
    return covariateId.equals(that.covariateId);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + covariateId.hashCode();
    return result;
  }
}
