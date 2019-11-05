package org.drugis.addis.problems.model;

import com.fasterxml.jackson.core.Version;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.util.WebConstants;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BenefitRiskProblem extends AbstractProblem {

  private Version schemaVersion;
  private Map<URI, CriterionEntry> criteria;
  private Map<String, AlternativeEntry> alternatives;
  private List<AbstractMeasurementEntry> performanceTable;

  public BenefitRiskProblem(Version schemaVersion, Map<URI, CriterionEntry> criteria, Map<String, AlternativeEntry> alternatives, List<AbstractMeasurementEntry> performanceTable) {
    this.schemaVersion = schemaVersion;
    this.criteria = criteria;
    this.alternatives = alternatives;
    this.performanceTable = performanceTable;
  }

  public BenefitRiskProblem(Map<URI, CriterionEntry> criteria, Map<String, AlternativeEntry> alternatives, List<AbstractMeasurementEntry> performanceTable) {
    this(WebConstants.SCHEMA_VERSION, criteria, alternatives, performanceTable);
  }

  public String getSchemaVersion() {
    return schemaVersion.toString(); // want to only get string when serialising to JSON
  }

  public Map<URI, CriterionEntry> getCriteria() {
    return criteria;
  }

  public Map<String, AlternativeEntry> getAlternatives() {
    return alternatives;
  }

  public List<AbstractMeasurementEntry> getPerformanceTable() {
    return performanceTable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BenefitRiskProblem that = (BenefitRiskProblem) o;
    return Objects.equals(schemaVersion, that.schemaVersion) &&
            Objects.equals(criteria, that.criteria) &&
            Objects.equals(alternatives, that.alternatives) &&
            Objects.equals(performanceTable, that.performanceTable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaVersion, criteria, alternatives, performanceTable);
  }
}
