package org.drugis.addis.problems.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.util.ObjectToStringDeserializer;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/21/14.
 */
@JsonDeserialize(using = ObjectToStringDeserializer.class)
public class SingleStudyBenefitRiskProblem extends AbstractProblem {
  private Map<URI, CriterionEntry> criteria;
  private Map<String, AlternativeEntry> alternatives;
  private List<AbstractMeasurementEntry> performanceTable;

  public SingleStudyBenefitRiskProblem(Map<String, AlternativeEntry> alternatives, Map<URI, CriterionEntry> criteria, List<AbstractMeasurementEntry> performanceTable) {
    this.alternatives = alternatives;
    this.criteria = criteria;
    this.performanceTable = performanceTable;
  }

  public Map<String, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  public Map<URI, CriterionEntry> getCriteria() {
    return criteria;
  }

  public List<AbstractMeasurementEntry> getPerformanceTable() {
    return performanceTable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SingleStudyBenefitRiskProblem that = (SingleStudyBenefitRiskProblem) o;

    if (!criteria.equals(that.criteria)) return false;
    if (!alternatives.equals(that.alternatives)) return false;
    return performanceTable.equals(that.performanceTable);
  }

  @Override
  public int hashCode() {
    int result = criteria.hashCode();
    result = 31 * result + alternatives.hashCode();
    result = 31 * result + performanceTable.hashCode();
    return result;
  }
}
