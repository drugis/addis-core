package org.drugis.addis.patavitask;

import java.net.URI;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTask {

  private URI id;

  private String method;

  private String problem;

  private boolean hasResult = false;

  public PataviTask() {
  }

  public PataviTask(URI id, String method, String problem, boolean hasResult) {
    this.id = id;
    this.method = method;
    this.problem = problem;
    this.hasResult = hasResult;
  }

  public PataviTask(URI id, String method, String problem) {
    this.id = id;
    this.method = method;
    this.problem = problem;
  }

  public PataviTask(String method, String problem) {
    this.method = method;
    this.problem = problem;
  }

  public URI getId() {
    return id;
  }

  public String getMethod() {
    return method;
  }

  public String getProblem() {
    return problem;
  }

  public boolean isHasResult() {
    return hasResult;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PataviTask that = (PataviTask) o;

    if (hasResult != that.hasResult) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!method.equals(that.method)) return false;
    return problem.equals(that.problem);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + method.hashCode();
    result = 31 * result + problem.hashCode();
    result = 31 * result + (hasResult ? 1 : 0);
    return result;
  }
}
