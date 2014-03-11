package org.drugis.addis.analyses;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

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

  public Analysis() {
  }

  public Analysis(Integer id, Integer projectId, String name, AnalysisType analysisType) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.analysisType = analysisType;
  }

  public Analysis(Integer projectId, String name, AnalysisType analysisType) {
    this(null, projectId, name, analysisType);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Analysis)) return false;

    Analysis analysis = (Analysis) o;

    if (analysisType != analysis.analysisType) return false;
    if (id != null ? !id.equals(analysis.id) : analysis.id != null) return false;
    if (!name.equals(analysis.name)) return false;
    if (!projectId.equals(analysis.projectId)) return false;
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
    return result;
  }
}