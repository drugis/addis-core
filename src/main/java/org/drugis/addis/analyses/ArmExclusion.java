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

  @ManyToOne(optional = false)
  @JoinColumn(name = "analysisId")
  @JsonIgnore
  private NetworkMetaAnalysis analysis;

  private String trialverseUid;

  public ArmExclusion() {
  }

  public ArmExclusion(NetworkMetaAnalysis analysis, String trialverseUid) {
    this.analysis = analysis;
    this.trialverseUid = trialverseUid;
  }

  public Integer getId() {
    return id;
  }

  public NetworkMetaAnalysis getAnalysis() {
    return analysis;
  }

  public String getTrialverseUid() {
    return trialverseUid;
  }

  public void setAnalysis(NetworkMetaAnalysis analysis) {
    this.analysis = analysis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArmExclusion that = (ArmExclusion) o;

    if (analysis != null ? !analysis.equals(that.analysis) : that.analysis != null) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!trialverseUid.equals(that.trialverseUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (analysis != null ? analysis.getId().hashCode() : 0);
    result = 31 * result + trialverseUid.hashCode();
    return result;
  }
}
