package org.drugis.addis.analyses;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by daan on 10-6-14.
 */
@Entity
public class ArmExclusion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer analysisId;

  private Long trialverseId;

  public ArmExclusion() {
  }

  public ArmExclusion(Integer analysisId, Long trialverseId) {
    this.analysisId = analysisId;
    this.trialverseId = trialverseId;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArmExclusion that = (ArmExclusion) o;

    if (!analysisId.equals(that.analysisId)) return false;
    if (!id.equals(that.id)) return false;
    if (!trialverseId.equals(that.trialverseId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + trialverseId.hashCode();
    return result;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public Long getTrialverseId() {
    return trialverseId;
  }

  public void setTrialverseId(Long trialverseId) {
    this.trialverseId = trialverseId;
  }
}
