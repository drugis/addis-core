package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.drugis.addis.outcomes.Outcome;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 6-5-14.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkMetaAnalysis extends AbstractAnalysis implements Serializable {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  private Integer id;
  private Integer projectId;
  private String title;
  private Integer primaryModel;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval = true)
  private List<ArmExclusion> excludedArms = new ArrayList<>();

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval = true)
  private List<InterventionInclusion> includedInterventions = new ArrayList<>();

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval = true)
  private List<CovariateInclusion> includedCovariates = new ArrayList<>();

  @ManyToOne(targetEntity = Outcome.class)
  @JoinColumn(name = "outcomeId")
  private Outcome outcome;

  public NetworkMetaAnalysis() {
  }

  public NetworkMetaAnalysis(Integer id, Integer projectId, String title, Outcome outcome) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.outcome = outcome;
  }

  public NetworkMetaAnalysis(Integer projectId, String title) {
    this.projectId = projectId;
    this.title = title;
  }

  public NetworkMetaAnalysis(Integer id, Integer projectId, String title) {
    this(id, projectId, title, null);
  }

  public NetworkMetaAnalysis(Integer id, Integer projectId, String title, List<ArmExclusion> excludedArms,
                             List<InterventionInclusion> includedInterventions, List<CovariateInclusion> includedCovariates, Outcome outcome) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.excludedArms = excludedArms == null ? new ArrayList<>() : excludedArms;
    this.includedInterventions = includedInterventions == null ? new ArrayList<>() : includedInterventions;
    this.includedCovariates = includedCovariates == null ? new ArrayList<>() : includedCovariates;
    this.outcome = outcome;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public Integer getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }

  public List<ArmExclusion> getExcludedArms() {
    return excludedArms;
  }

  public List<InterventionInclusion> getIncludedInterventions() {
    return includedInterventions;
  }

  public List<CovariateInclusion> getCovariateInclusions() {
    return includedCovariates;
  }

  public Outcome getOutcome() {
    return outcome;
  }

  public Integer getPrimaryModel() {
    return primaryModel;
  }

  public void setPrimaryModel(Integer primaryModel) {
    this.primaryModel = primaryModel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkMetaAnalysis that = (NetworkMetaAnalysis) o;

    if (!id.equals(that.id)) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!title.equals(that.title)) return false;
    if (primaryModel != null ? !primaryModel.equals(that.primaryModel) : that.primaryModel != null)
      return false;
    return outcome != null ? outcome.equals(that.outcome) : that.outcome == null;

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (primaryModel != null ? primaryModel.hashCode() : 0);
    result = 31 * result + (outcome != null ? outcome.hashCode() : 0);
    return result;
  }
}