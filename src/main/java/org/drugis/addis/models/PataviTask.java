package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by connor on 26-6-14.
 */
@Entity
public class PataviTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer modelId;

  private String method;

  @JsonRawValue
  private String problem;

  public PataviTask() {
  }

  public PataviTask(Integer modelId, String method, String problem) {
    this.modelId = modelId;
    this.method = method;
    this.problem = problem;
  }

  public Integer getId() {
    return id;
  }

  public Integer getModelId() {
    return modelId;
  }

  public String getMethod() {
    return method;
  }

  public String getProblem() {
    return problem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PataviTask)) return false;

    PataviTask that = (PataviTask) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!method.equals(that.method)) return false;
    if (!modelId.equals(that.modelId)) return false;
    if (!problem.equals(that.problem)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + modelId.hashCode();
    result = 31 * result + method.hashCode();
    result = 31 * result + problem.hashCode();
    return result;
  }
}
