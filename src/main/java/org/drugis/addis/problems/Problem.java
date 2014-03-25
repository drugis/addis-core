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
  Map<String, CriterionEntry> criteria;

  public Problem() {
  }

  public Problem(String title, Map<String, AlternativeEntry> alternatives, Map<String, CriterionEntry> criteria) {
    this.title = title;
    this.alternatives = alternatives;
    this.criteria = criteria;
  }

  public String getTitle() {
    return title;
  }

  public Map<String, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  public Map<String, CriterionEntry> getCriteria() {
    return criteria;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Problem)) return false;

    Problem problem = (Problem) o;

    if (!alternatives.equals(problem.alternatives)) return false;
    if (!criteria.equals(problem.criteria)) return false;
    if (!title.equals(problem.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + alternatives.hashCode();
    result = 31 * result + criteria.hashCode();
    return result;
  }
}
