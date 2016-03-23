package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.drugis.addis.outcomes.Outcome;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

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

  @JsonProperty("excludedArms")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysisId", orphanRemoval = true)
  private Set<ArmExclusion> excludedArms = new HashSet<>();

  @JsonProperty("includedInterventions")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysisId", orphanRemoval = true)
  private Set<InterventionInclusion> includedInterventions = new HashSet<>();

  @JsonProperty("includedCovariates")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysisId", orphanRemoval = true)
  private Set<CovariateInclusion> includedCovariates = new HashSet<>();

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

  public NetworkMetaAnalysis(Integer id, Integer projectId, String title,
                             List<ArmExclusion> excludedArms,
                             List<InterventionInclusion> includedInterventions,
                             List<CovariateInclusion> includedCovariates, Outcome outcome) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.excludedArms = excludedArms == null ? new HashSet<>() : new HashSet<>(excludedArms);
    this.includedInterventions = includedInterventions == null ? new HashSet<>() : new HashSet<>(includedInterventions);
    this.includedCovariates = includedCovariates == null ? new HashSet<>() : new HashSet<>(includedCovariates);
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

  @JsonIgnore
  public List<ArmExclusion> getExcludedArms() {
    return Collections.unmodifiableList(new ArrayList<>(excludedArms));
  }

  @JsonIgnore
  public List<InterventionInclusion> getIncludedInterventions() {
    return Collections.unmodifiableList(new ArrayList<>(includedInterventions));
  }

  @JsonIgnore
  public List<CovariateInclusion> getCovariateInclusions() {
    return Collections.unmodifiableList(new ArrayList<>(includedCovariates));
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

  public void updateArmExclusions(Set<ArmExclusion> excludedArms){
    updateSet(this.excludedArms, excludedArms);
  }

  public void updateIncludedInterventions(Set<InterventionInclusion> includedInterventions){
    updateSet(this.includedInterventions, includedInterventions);
  }

  public void updateIncludedCovariates(Set<CovariateInclusion> includedCovariates){
    updateSet(this.includedCovariates, includedCovariates);
  }

  private <T> void updateSet(Set<T> oldSet, Set<T> newSet){
    Set<T> removeSet = new HashSet<>();
    for(T oldItem : oldSet) {
      if(!newSet.contains(oldItem)){
        removeSet.add(oldItem);
      }
    }
    oldSet.removeAll(removeSet);
    oldSet.addAll(newSet);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NetworkMetaAnalysis)) return false;

    NetworkMetaAnalysis that = (NetworkMetaAnalysis) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!title.equals(that.title)) return false;
    if (primaryModel != null ? !primaryModel.equals(that.primaryModel) : that.primaryModel != null) return false;
    if (!excludedArms.equals(that.excludedArms)) return false;
    if (!includedInterventions.equals(that.includedInterventions)) return false;
    if (!includedCovariates.equals(that.includedCovariates)) return false;
    return outcome != null ? outcome.equals(that.outcome) : that.outcome == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (primaryModel != null ? primaryModel.hashCode() : 0);
    result = 31 * result + excludedArms.hashCode();
    result = 31 * result + includedInterventions.hashCode();
    result = 31 * result + includedCovariates.hashCode();
    result = 31 * result + (outcome != null ? outcome.hashCode() : 0);
    return result;
  }
}