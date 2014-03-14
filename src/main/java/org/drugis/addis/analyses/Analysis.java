package org.drugis.addis.analyses;

import org.drugis.addis.outcomes.Outcome;
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

  @Type(type = "org.drugis.addis.analyses.AnalysisTypeUserType")
  private AnalysisType analysisType;
  private String study;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name="analysis_outcomes",
    joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "outcomeId", referencedColumnName = "id")})
  private List<Outcome> selectedOutcomes;

  public Analysis() {
  }

  public Analysis(Integer id, Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.analysisType = analysisType;
    this.selectedOutcomes = selectedOutcomes == null ? this.selectedOutcomes : selectedOutcomes;
  }

  public Analysis(Integer projectId, String name, AnalysisType analysisType, List<Outcome> selectedOutcomes) {
    this(null, projectId, name, analysisType, selectedOutcomes);
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

  public String getStudy() {
    return study;
  }

  public List<Outcome> getSelectedOutcomes() {
    return selectedOutcomes == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(selectedOutcomes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Analysis analysis = (Analysis) o;

    if (analysisType != analysis.analysisType) return false;
    if (id != null ? !id.equals(analysis.id) : analysis.id != null) return false;
    if (!name.equals(analysis.name)) return false;
    if (!projectId.equals(analysis.projectId)) return false;
    if (!selectedOutcomes.equals(analysis.selectedOutcomes)) return false;
    if (study != null ? !study.equals(analysis.study) : analysis.study != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + analysisType.hashCode();
    result = 31 * result + (study != null ? study.hashCode() : 0);
    // result = 31 * result + (selectedOutcomes != null ? selectedOutcomes.hashCode() : 0);
    return result;
  }
}