package org.drugis.addis.analyses.model;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.net.URI;

/**
 * Created by joris on 13-6-17.
 */
@Entity
@IdClass(BenefitRiskStudyOutcomeInclusion.BenefitRiskStudyOutcomeInclusionPK.class)
public class BenefitRiskStudyOutcomeInclusion {
  public static class BenefitRiskStudyOutcomeInclusionPK  {
    protected Integer analysisId;
    protected Integer outcomeId;
    protected URI studyGraphUri;

    public BenefitRiskStudyOutcomeInclusionPK() {
    }

    public BenefitRiskStudyOutcomeInclusionPK(Integer analysisId, Integer outcomeId, URI studyGraphUri) {
      this.analysisId = analysisId;
      this.outcomeId = outcomeId;
      this.studyGraphUri = studyGraphUri;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      BenefitRiskStudyOutcomeInclusionPK that = (BenefitRiskStudyOutcomeInclusionPK) o;

      if (!analysisId.equals(that.analysisId)) return false;
      if (!outcomeId.equals(that.outcomeId)) return false;
      return studyGraphUri != null ? studyGraphUri.equals(that.studyGraphUri) : that.studyGraphUri == null;
    }

    @Override
    public int hashCode() {
      int result = analysisId.hashCode();
      result = 31 * result + outcomeId.hashCode();
      result = 31 * result + (studyGraphUri != null ? studyGraphUri.hashCode() : 0);
      return result;
    }
  }

  @Id
  private Integer analysisId;

  @Id
  private Integer outcomeId;
  @Id
  @Type(type = "org.drugis.addis.util.UriUserType")
  private URI studyGraphUri;

  public BenefitRiskStudyOutcomeInclusion(Integer analysisId, Integer outcomeId, URI studyGraphUri) {
    this.analysisId = analysisId;
    this.outcomeId = outcomeId;
    this.studyGraphUri = studyGraphUri;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getOutcomeId() {
    return outcomeId;
  }

  public URI getStudyGraphUri() {
    return studyGraphUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BenefitRiskStudyOutcomeInclusion that = (BenefitRiskStudyOutcomeInclusion) o;

    if (!analysisId.equals(that.analysisId)) return false;
    if (!outcomeId.equals(that.outcomeId)) return false;
    return studyGraphUri != null ? studyGraphUri.equals(that.studyGraphUri) : that.studyGraphUri == null;
  }

  @Override
  public int hashCode() {
    int result = analysisId.hashCode();
    result = 31 * result + outcomeId.hashCode();
    result = 31 * result + (studyGraphUri != null ? studyGraphUri.hashCode() : 0);
    return result;
  }
}
