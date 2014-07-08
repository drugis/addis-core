package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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

  private Long trialverseId;

  public ArmExclusion() {
  }

  public ArmExclusion(NetworkMetaAnalysis analysis, Long trialverseId) {
    this.analysis = analysis;
    this.trialverseId = trialverseId;
  }

  public Integer getId() {
    return id;
  }

  public NetworkMetaAnalysis getAnalysis() {
    return analysis;
  }

  public Long getTrialverseId() {
    return trialverseId;
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
    if (!trialverseId.equals(that.trialverseId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (analysis != null ? analysis.getId().hashCode() : 0);
    result = 31 * result + trialverseId.hashCode();
    return result;
  }
}
