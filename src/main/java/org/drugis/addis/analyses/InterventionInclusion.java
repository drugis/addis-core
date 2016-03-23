package org.drugis.addis.analyses;

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

  private Integer analysisId;

  private Integer interventionId;

  public InterventionInclusion() {
  }

  public InterventionInclusion(Integer analysisId, Integer interventionId) {
    this.analysisId = analysisId;
    this.interventionId = interventionId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getInterventionId() {
    return interventionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InterventionInclusion that = (InterventionInclusion) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!analysisId.equals(that.analysisId)) return false;
    return interventionId.equals(that.interventionId);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + interventionId.hashCode();
    return result;
  }
}
