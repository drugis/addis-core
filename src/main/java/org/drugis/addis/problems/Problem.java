package org.drugis.addis.problems;

import javax.persistence.Entity;
import java.util.Map;

/**
 * Created by daan on 3/21/14.
 */
@Entity
public class Problem {
  String title;
  Map<String, AlternativeEntry> alternatives;

  public Problem(String title, Map<String, AlternativeEntry> alternatives) {
    this.title = title;
    this.alternatives = alternatives;
  }

  public String getTitle() {
    return title;
  }

  public Map<String, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Problem problem = (Problem) o;

    if (!alternatives.equals(problem.alternatives)) return false;
    if (!title.equals(problem.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + alternatives.hashCode();
    return result;
  }
}
