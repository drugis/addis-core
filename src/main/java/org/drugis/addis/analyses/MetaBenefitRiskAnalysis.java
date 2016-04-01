package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import org.drugis.addis.interventions.model.Intervention;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by daan on 24-2-16.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaBenefitRiskAnalysis extends AbstractAnalysis implements Serializable {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  private Integer id;

  private Integer projectId;

  private String title;

  private boolean finalized = false;

  @JsonRawValue
  private String problem;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinTable(name = "MetaBenefitRiskAnalysis_Alternative",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "alternativeId", referencedColumnName = "id")})
  private Set<Intervention> includedAlternatives = new HashSet<>();
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "metaBenefitRiskAnalysisId", cascade = CascadeType.ALL)
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

  public MetaBenefitRiskAnalysis(Integer id, Integer projectId, String title, Set<Intervention> includedAlternatives) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.includedAlternatives = includedAlternatives;
  }

  @Override
  public Integer getProjectId() {
    return projectId;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public List<Intervention> getIncludedAlternatives() {
    return Collections.unmodifiableList(new ArrayList<>(includedAlternatives));
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

  public void setIncludedAlternatives(List<Intervention> interventions) {
    this.includedAlternatives = new HashSet<>(interventions);
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
    if (!includedAlternatives.equals(that.includedAlternatives)) return false;
    return mbrOutcomeInclusions.equals(that.mbrOutcomeInclusions);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + includedAlternatives.hashCode();
    result = 31 * result + mbrOutcomeInclusions.hashCode();
    result = 31 * result + (finalized ? 1 : 0);
    return result;
  }

}
