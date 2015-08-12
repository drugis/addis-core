package org.drugis.addis.analyses;

import org.drugis.addis.outcomes.Outcome;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 6-5-14.
 */
@Entity
public class NetworkMetaAnalysis extends AbstractAnalysis {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  private Integer id;
  private Integer projectId;
  private String title;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval = true)
  private List<ArmExclusion> excludedArms = new ArrayList<>();

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysis", orphanRemoval = true)
  private List<InterventionInclusion> includedInterventions = new ArrayList<>();

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

  public NetworkMetaAnalysis(Integer id, Integer projectId, String title, List<ArmExclusion> excludedArms, List<InterventionInclusion> includedInterventions, Outcome outcome) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.excludedArms = excludedArms == null ? new ArrayList<ArmExclusion>() : excludedArms;
    this.includedInterventions = includedInterventions == null ? new ArrayList<InterventionInclusion>() : includedInterventions;
    this.outcome = outcome;
  }

  public Integer getId() {
    return id;
  }

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

  public Outcome getOutcome() {
    return outcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkMetaAnalysis that = (NetworkMetaAnalysis) o;

    if (!excludedArms.equals(that.excludedArms)) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!title.equals(that.title)) return false;
    if (outcome != null ? !outcome.equals(that.outcome) : that.outcome != null) return false;
    if (!projectId.equals(that.projectId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + excludedArms.hashCode();
    result = 31 * result + (outcome != null ? outcome.hashCode() : 0);
    return result;
  }
}