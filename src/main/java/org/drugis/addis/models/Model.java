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
  private String title;
  private String linearModel;

  public Model() {
  }

  public Model(Integer taskId, Integer id, Integer analysisId, String title, String linearModel) {
    this.taskId = taskId;
    this.id = id;
    this.analysisId = analysisId;
    this.title = title;
    this.linearModel = linearModel;
  }

  public Model(Integer id, Integer analysisId, String title, String linearModel) {
    this.id = id;
    this.analysisId = analysisId;
    this.title = title;
    this.linearModel = linearModel;
  }

  public Model(Integer analysisId, String title, String linearModel) {
    this.analysisId = analysisId;
    this.title = title;
    this.linearModel = linearModel;
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

  public String getTitle() {
    return title;
  }

  public String getLinearModel() {
    return linearModel;
  }

  public void setTaskId(Integer taskId) {
    this.taskId = taskId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Model model = (Model) o;

    if (!id.equals(model.id)) return false;
    if (!analysisId.equals(model.analysisId)) return false;
    if (taskId != null ? !taskId.equals(model.taskId) : model.taskId != null) return false;
    if (!title.equals(model.title)) return false;
    return linearModel.equals(model.linearModel);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
    result = 31 * result + title.hashCode();
    result = 31 * result + linearModel.hashCode();
    return result;
  }
}
