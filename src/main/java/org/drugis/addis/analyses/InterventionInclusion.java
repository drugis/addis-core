package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by connor on 18-6-14.
 */
@Entity
public class InterventionInclusion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "analysisId")
  @JsonIgnore
  private NetworkMetaAnalysis analysis;

  private Integer interventionId;

  public InterventionInclusion() {
  }

  public InterventionInclusion(NetworkMetaAnalysis analysis, Integer interventionId) {
    this.analysis = analysis;
    this.interventionId = interventionId;
  }

  public Integer getId() {
    return id;
  }

  public NetworkMetaAnalysis getAnalysis() {
    return analysis;
  }

  public Integer getInterventionId() {
    return interventionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InterventionInclusion)) return false;

    InterventionInclusion that = (InterventionInclusion) o;

    if (!analysis.equals(that.analysis)) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!interventionId.equals(that.interventionId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysis.hashCode();
    result = 31 * result + interventionId.hashCode();
    return result;
  }

  public void setAnalysis(NetworkMetaAnalysis analysis) {
    this.analysis = analysis;
  }
}
