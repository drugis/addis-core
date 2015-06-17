package org.drugis.addis.patavitask;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTask {

  private Integer id;

  private String method;

  private String problem;

  public PataviTask() {
  }

  public PataviTask(Integer id, String method, String problem) {
    this.id = id;
    this.method = method;
    this.problem = problem;
  }

  public PataviTask(String method, String problem) {
    this.method = method;
    this.problem = problem;
  }

  public Integer getId() {
    return id;
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
    if (!problem.equals(that.problem)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + method.hashCode();
    result = 31 * result + problem.hashCode();
    return result;
  }
}
