package org.drugis.addis.problems.model;

import com.google.common.collect.ImmutableSet;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CriteriaAndMeasurements {
  private HashMap<URI, CriterionEntry> criteria = new HashMap<>();
  private Set<MeasurementWithCoordinates> measurements = new HashSet<>();

  public HashMap<URI, CriterionEntry> getCriteria() {
    return criteria;
  }

  public Set<MeasurementWithCoordinates> getMeasurements() {
    return ImmutableSet.copyOf(measurements);
  }

  public void addCriterionEntry(URI variableConceptUri, CriterionEntry criterionEntry) {
    criteria.put(variableConceptUri, criterionEntry);
  }

  public void addMeasurementWithCoordinates(MeasurementWithCoordinates measurementWithCoordinates) {
    measurements.add(measurementWithCoordinates);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CriteriaAndMeasurements that = (CriteriaAndMeasurements) o;
    return Objects.equals(criteria, that.criteria) &&
            Objects.equals(measurements, that.measurements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(criteria, measurements);
  }
}
