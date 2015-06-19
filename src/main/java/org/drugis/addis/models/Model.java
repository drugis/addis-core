package org.drugis.addis.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 22-5-14.
 */
@Entity
public class Model {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer analysisId;

  private Integer taskId;

  public Model() {
  }

  public Model(Integer taskId, Integer id, Integer analysisId) {
    this.taskId = taskId;
    this.id = id;
    this.analysisId = analysisId;
  }

  public Model(Integer id, Integer analysisId) {
    this.id = id;
    this.analysisId = analysisId;
  }

  public Model(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getTaskId() {
    return taskId;
  }

  public void setTaskId(Integer taskId) {
    this.taskId = taskId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Model)) return false;

    Model model = (Model) o;

    if (analysisId != null ? !analysisId.equals(model.analysisId) : model.analysisId != null) return false;
    if (!id.equals(model.id)) return false;
    if (taskId != null ? !taskId.equals(model.taskId) : model.taskId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + (analysisId != null ? analysisId.hashCode() : 0);
    result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
    return result;
  }
}
