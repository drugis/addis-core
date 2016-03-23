package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by daan on 10-6-14.
 */
@Entity
public class ArmExclusion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;

  private Integer analysisId;

  private String trialverseUid;

  public ArmExclusion() {
  }

  public ArmExclusion(Integer analysisId, String trialverseUid) {
    this.analysisId = analysisId;
    this.trialverseUid = trialverseUid;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public String getTrialverseUid() {
    return trialverseUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArmExclusion)) return false;

    ArmExclusion that = (ArmExclusion) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!analysisId.equals(that.analysisId)) return false;
    return trialverseUid.equals(that.trialverseUid);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + trialverseUid.hashCode();
    return result;
  }
}
