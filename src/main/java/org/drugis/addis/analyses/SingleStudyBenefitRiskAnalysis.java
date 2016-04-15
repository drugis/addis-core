package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.util.ObjectToStringDeserializer;
import org.drugis.trialverse.util.Utils;

import javax.persistence.*;
import java.util.*;

/**
 * Created by connor on 3/11/14.
 */
@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class SingleStudyBenefitRiskAnalysis extends AbstractAnalysis {

  @JsonRawValue
  private String problem;

  private String studyGraphUid;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name = "singleStudyBenefitRiskAnalysis_Outcome",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "outcomeId", referencedColumnName = "id")})
  private Set<Outcome> selectedOutcomes = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "analysisId", orphanRemoval = true)
  private Set<InterventionInclusion> selectedInterventions = new HashSet<>();

  public SingleStudyBenefitRiskAnalysis() {
  }

  public SingleStudyBenefitRiskAnalysis(Integer id, Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> selectedInterventions, String problem) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.selectedOutcomes = selectedOutcomes == null ? this.selectedOutcomes : new HashSet<>(selectedOutcomes);
    this.selectedInterventions = selectedInterventions == null ? this.selectedInterventions : new HashSet<>(selectedInterventions);
    this.problem = problem;
  }

  public SingleStudyBenefitRiskAnalysis(Integer id, Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> selectedInterventions) {
    this(id, projectId, title, selectedOutcomes, selectedInterventions, null);
  }

  public SingleStudyBenefitRiskAnalysis(Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> selectedInterventions) {
    this(null, projectId, title, selectedOutcomes, selectedInterventions, null);
  }

  public SingleStudyBenefitRiskAnalysis(Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> selectedInterventions, String problem) {
    this(null, projectId, title, selectedOutcomes, selectedInterventions, problem);
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

  public String getStudyGraphUid() {
    return studyGraphUid;
  }

  public void setStudyGraphUid(String studyGraphUid) {
    this.studyGraphUid = studyGraphUid;
  }

  public List<Outcome> getSelectedOutcomes() {
    return selectedOutcomes == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList<>(selectedOutcomes));
  }

  public List<InterventionInclusion> getSelectedInterventions() {
    return selectedInterventions == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList<>(selectedInterventions));
  }

  public void updateSelectedOutcomes(List<Outcome> newOutcomes){
    Utils.updateSet(this.selectedOutcomes, new HashSet<>(newOutcomes));
  }

  public void updateSelectedInterventions(List<InterventionInclusion> newInterventionInclusions) {
    Utils.updateSet(this.selectedInterventions, new HashSet<>(newInterventionInclusions));
  }


  @JsonRawValue
  public String getProblem() {
    return problem;
  }

  @JsonDeserialize(using = ObjectToStringDeserializer.class)
  public void setProblem(String problem) {
    this.problem = problem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SingleStudyBenefitRiskAnalysis analysis = (SingleStudyBenefitRiskAnalysis) o;

    if (id != null ? !id.equals(analysis.id) : analysis.id != null) return false;
    if (!title.equals(analysis.title)) return false;
    if (problem != null ? !problem.equals(analysis.problem) : analysis.problem != null) return false;
    if (!projectId.equals(analysis.projectId)) return false;
    if (!selectedInterventions.equals(analysis.selectedInterventions)) return false;
    if (!selectedOutcomes.equals(analysis.selectedOutcomes)) return false;
    if (studyGraphUid != null ? !studyGraphUid.equals(analysis.studyGraphUid) : analysis.studyGraphUid != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (problem != null ? problem.hashCode() : 0);
    result = 31 * result + (studyGraphUid != null ? studyGraphUid.hashCode() : 0);
    result = 31 * result + selectedOutcomes.hashCode();
    result = 31 * result + selectedInterventions.hashCode();
    return result;
  }

}