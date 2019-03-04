package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Objects;

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
    if (o == null || getClass() != o.getClass()) return false;
    CovariateInclusion that = (CovariateInclusion) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(analysisId, that.analysisId) &&
            Objects.equals(covariateId, that.covariateId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, analysisId, covariateId);
  }
}
