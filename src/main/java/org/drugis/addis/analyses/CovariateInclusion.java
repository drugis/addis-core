package org.drugis.addis.analyses;

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

  @ManyToOne(optional = false)
  @JoinColumn(name = "analysisId")
  @JsonIgnore
  private NetworkMetaAnalysis analysis;

  private Integer covariateId;

  public CovariateInclusion() {
  }

  public CovariateInclusion(NetworkMetaAnalysis analysis, Integer covariateId) {
    this.analysis = analysis;
    this.covariateId = covariateId;
  }

  public Integer getId() {
    return id;
  }

  public NetworkMetaAnalysis getAnalysis() {
    return analysis;
  }

  public void setAnalysis(NetworkMetaAnalysis analysis) {
    this.analysis = analysis;
  }

  public Integer getCovariateId() {
    return covariateId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CovariateInclusion)) return false;

    CovariateInclusion that = (CovariateInclusion) o;

    if (!analysis.equals(that.analysis)) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    return covariateId.equals(that.covariateId);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysis.hashCode();
    result = 31 * result + covariateId.hashCode();
    return result;
  }
}
