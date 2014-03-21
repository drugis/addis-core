package org.drugis.addis.problem;

import javax.persistence.Entity;

/**
 * Created by daan on 3/21/14.
 */
@Entity
public class Problem {
  String title;

  public Problem(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Problem problem = (Problem) o;

    if (title != null ? !title.equals(problem.title) : problem.title != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return title != null ? title.hashCode() : 0;
  }
}
