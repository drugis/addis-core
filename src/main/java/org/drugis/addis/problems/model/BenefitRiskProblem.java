package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 4-3-16.
 */
public class BenefitRiskProblem extends AbstractProblem {

  private Map<URI, CriterionEntry> criteria;
  private Map<Integer, AlternativeEntry> alternatives;
  private List<AbstractMeasurementEntry> performanceTable;

  public BenefitRiskProblem(Map<URI, CriterionEntry> criteria, Map<Integer, AlternativeEntry> alternatives, List<AbstractMeasurementEntry> performanceTable) {
    this.criteria = criteria;
    this.alternatives = alternatives;
    this.performanceTable = performanceTable;
  }

  public Map<URI, CriterionEntry> getCriteria() {
    return criteria;
  }

  public Map<Integer, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  public List<AbstractMeasurementEntry> getPerformanceTable() {
    return performanceTable;
  }
}
