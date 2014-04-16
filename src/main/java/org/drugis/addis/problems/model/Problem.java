package org.drugis.addis.problems.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.util.ObjectToStringDeserializer;

import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/21/14.
 */
@JsonDeserialize(using = ObjectToStringDeserializer.class)
public class Problem {
  private String title;
  private Map<String, AlternativeEntry> alternatives;
  private Map<String, CriterionEntry> criteria;
  private List<AbstractMeasurementEntry> performanceTable;

  public Problem(String title, Map<String, AlternativeEntry> alternatives, Map<String, CriterionEntry> criteria, List<AbstractMeasurementEntry> performanceTable) {
    this.title = title;
    this.alternatives = alternatives;
    this.criteria = criteria;
    this.performanceTable = performanceTable;
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

  public List<AbstractMeasurementEntry> getPerformanceTable() {
    return performanceTable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Problem problem = (Problem) o;

    if (!alternatives.equals(problem.alternatives)) return false;
    if (!criteria.equals(problem.criteria)) return false;
    if (!performanceTable.equals(problem.performanceTable)) return false;
    if (!title.equals(problem.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + alternatives.hashCode();
    result = 31 * result + criteria.hashCode();
    result = 31 * result + performanceTable.hashCode();
    return result;
  }
}
