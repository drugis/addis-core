package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by daan on 24-2-16.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class MetaBenefitRiskAnalysis extends AbstractAnalysis implements Serializable {
  private boolean finalized = false;

  @JsonRawValue
  private String problem;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "metaBenefitRiskAnalysisId", cascade = CascadeType.ALL)
  private Set<MbrOutcomeInclusion> mbrOutcomeInclusions = new HashSet<>();

  public MetaBenefitRiskAnalysis() {
  }

  public MetaBenefitRiskAnalysis(Integer projectId, String title) {
    this.projectId = projectId;
    this.title = title;
  }

  public MetaBenefitRiskAnalysis(Integer id, Integer projectId, String title) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
  }

  public MetaBenefitRiskAnalysis(Integer id, Integer projectId, String title, Set<InterventionInclusion> interventionInclusions) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.interventionInclusions = interventionInclusions;
  }

  @Override
  public Integer getProjectId() {
    return projectId;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public List<MbrOutcomeInclusion> getMbrOutcomeInclusions() {
    return Collections.unmodifiableList(new ArrayList<>(mbrOutcomeInclusions));
  }

  public String getTitle() {
    return title;
  }

  @JsonRawValue
  public String getProblem() {
    return problem;
  }

  public boolean isFinalized() {
    return finalized;
  }
  public void setFinalized(boolean finalized) {
    this.finalized = finalized;
  }

  public void setProblem(String problem) {
    this.problem = problem;
  }

  public void setMbrOutcomeInclusions(List<MbrOutcomeInclusion> mbrOutcomeInclusions) {
    this.mbrOutcomeInclusions = new HashSet<>(mbrOutcomeInclusions);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MetaBenefitRiskAnalysis that = (MetaBenefitRiskAnalysis) o;

    if (finalized != that.finalized) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!title.equals(that.title)) return false;
    if (!interventionInclusions.equals(that.interventionInclusions)) return false;
    return mbrOutcomeInclusions.equals(that.mbrOutcomeInclusions);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + interventionInclusions.hashCode();
    result = 31 * result + mbrOutcomeInclusions.hashCode();
    result = 31 * result + (finalized ? 1 : 0);
    return result;
  }

}
