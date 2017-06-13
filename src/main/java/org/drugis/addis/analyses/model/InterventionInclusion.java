package org.drugis.addis.analyses.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * Created by connor on 18-6-14.
 */
@Entity
@IdClass(InterventionInclusion.PK.class)
public class InterventionInclusion {

  static class PK implements Serializable {
    protected Integer analysisId;
    protected Integer interventionId;

    public PK() {
    }

    public PK(Integer analysisId, Integer interventionId) {
      this.analysisId = analysisId;
      this.interventionId = interventionId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PK pk = (PK) o;

      if (!analysisId.equals(pk.analysisId)) return false;
      return interventionId.equals(pk.interventionId);

    }

    @Override
    public int hashCode() {
      int result = analysisId.hashCode();
      result = 31 * result + interventionId.hashCode();
      return result;
    }
  }

  @Id
  private Integer analysisId;

  @Id
  private Integer interventionId;

  public InterventionInclusion() {
  }

  public InterventionInclusion(Integer analysisId, Integer interventionId) {
    this.analysisId = analysisId;
    this.interventionId = interventionId;
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

    if (!analysisId.equals(that.analysisId)) return false;
    return interventionId.equals(that.interventionId);

  }

  @Override
  public int hashCode() {
    int result = analysisId.hashCode();
    result = 31 * result + interventionId.hashCode();
    return result;
  }
}
