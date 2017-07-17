package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import org.drugis.trialverse.util.Utils;

import javax.persistence.*;
import java.util.*;

/**
 * Created by connor on 6-5-14.
 */
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "analysisType")
@JsonSubTypes({@Type(value = NetworkMetaAnalysis.class, name = AnalysisType.EVIDENCE_SYNTHESIS),
        @Type(value = BenefitRiskAnalysis.class, name = AnalysisType.BENEFIT_RISK_ANALYSIS_LABEL)})
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
public abstract class AbstractAnalysis {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  protected Integer id;

  protected Integer projectId;

  protected String title;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysisId", orphanRemoval = true)
  protected Set<InterventionInclusion> interventionInclusions = new HashSet<>();

  private Boolean isArchived = false;

  @Column(name = "archived_on")
  @org.hibernate.annotations.Type(type = "date")
  private Date archivedOn;

  public Integer getId() {
    return id;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }
  public Boolean getArchived() {
    return isArchived;
  }

  public Date getArchivedOn() {
    return archivedOn;
  }

  public void setArchived(Boolean isArchived) {
    this.isArchived = isArchived;
  }

  public void setArchivedOn(Date archivedOn) {
    this.archivedOn = archivedOn;
  }

  public List<InterventionInclusion> getInterventionInclusions() {
    return interventionInclusions == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList<>(interventionInclusions));
  }

  public void updateIncludedInterventions(Set<InterventionInclusion> includedInterventions){
    Utils.updateSet(this.interventionInclusions, includedInterventions);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractAnalysis analysis = (AbstractAnalysis) o;

    if (id != null ? !id.equals(analysis.id) : analysis.id != null) return false;
    if (!projectId.equals(analysis.projectId)) return false;
    if (!title.equals(analysis.title)) return false;
    if (!interventionInclusions.equals(analysis.interventionInclusions)) return false;
    if (!isArchived.equals(analysis.isArchived)) return false;
    return archivedOn != null ? archivedOn.equals(analysis.archivedOn) : analysis.archivedOn == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + interventionInclusions.hashCode();
    result = 31 * result + isArchived.hashCode();
    result = 31 * result + (archivedOn != null ? archivedOn.hashCode() : 0);
    return result;
  }
}
