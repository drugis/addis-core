package org.drugis.addis.analyses;

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
@JsonSubTypes({@Type(value = SingleStudyBenefitRiskAnalysis.class, name = AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL),
        @Type(value = NetworkMetaAnalysis.class, name = AnalysisType.EVIDENCE_SYNTHESIS),
        @Type(value = MetaBenefitRiskAnalysis.class, name = AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL)})
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
}
