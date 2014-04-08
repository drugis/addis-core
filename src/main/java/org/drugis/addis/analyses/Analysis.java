package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.ProblemDeserializer;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by connor on 3/11/14.
 */
@Entity
public class Analysis implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer projectId;
  private String name;

  private String problem;

  @Type(type = "org.drugis.addis.analyses.AnalysisTypeUserType")
  private AnalysisType analysisType;

  private Integer studyId;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name = "analysis_outcomes",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "outcomeId", referencedColumnName = "id")})
  private List<Outcome> selectedOutcomes;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name = "analysis_interventions",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "interventionId", referencedColumnName = "id")})
  private List<Intervention> selectedInterventions;

  public Analysis() {
  }

  public Analysis(Integer id, Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes, List<Intervention> selectedInterventions, String problem) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.analysisType = analysisType;
    this.selectedOutcomes = selectedOutcomes == null ? this.selectedOutcomes : selectedOutcomes;
    this.selectedInterventions = selectedInterventions == null ? this.selectedInterventions : selectedInterventions;
    this.problem = problem;
  }

  public Analysis(Integer id, Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes, List<Intervention> selectedInterventions) {
    this(id, projectId, name, analysisType, selectedOutcomes, selectedInterventions, null);
  }

  public Analysis(Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes, List<Intervention> selectedInterventions) {
    this(null, projectId, name, analysisType, selectedOutcomes, selectedInterventions, null);
  }

  public Analysis(Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes, List<Intervention> selectedInterventions, String problem) {
    this(null, projectId, name, analysisType, selectedOutcomes, selectedInterventions, problem);
  }

  public Integer getId() {
    return id;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public AnalysisType getAnalysisType() {
    return analysisType;
  }

  public Integer getStudyId() {
    return studyId;
  }

  public List<Outcome> getSelectedOutcomes() {
    return selectedOutcomes == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(selectedOutcomes);
  }

  public List<Intervention> getSelectedInterventions() {
    return selectedInterventions == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(selectedInterventions);
  }

  public void setStudyId(Integer studyId) {
    this.studyId = studyId;
  }

  @JsonRawValue
  public String getProblem() {
    return problem;
  }

  @JsonDeserialize(using = ProblemDeserializer.class)
  public void setProblem(String problem) {
    this.problem = problem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Analysis analysis = (Analysis) o;

    if (analysisType != analysis.analysisType) return false;
    if (id != null ? !id.equals(analysis.id) : analysis.id != null) return false;
    if (!name.equals(analysis.name)) return false;
    if (problem != null ? !problem.equals(analysis.problem) : analysis.problem != null) return false;
    if (!projectId.equals(analysis.projectId)) return false;
    if (!selectedInterventions.equals(analysis.selectedInterventions)) return false;
    if (!selectedOutcomes.equals(analysis.selectedOutcomes)) return false;
    if (studyId != null ? !studyId.equals(analysis.studyId) : analysis.studyId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (problem != null ? problem.hashCode() : 0);
    result = 31 * result + analysisType.hashCode();
    result = 31 * result + (studyId != null ? studyId.hashCode() : 0);
    result = 31 * result + selectedOutcomes.hashCode();
    result = 31 * result + selectedInterventions.hashCode();
    return result;
  }
}