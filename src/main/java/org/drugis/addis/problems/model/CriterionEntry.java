package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String criterion;
  private String title;
  private String unitOfMeasurement;
  private List<DataSourceEntry> dataSources;

  public CriterionEntry(String criterion, String title, String source, URI sourceLink) {
    this(criterion, Collections.singletonList(new DataSourceEntry(source, sourceLink)), title, null);
  }

  public CriterionEntry(String criterion, String title, List<Double> scale, PartialValueFunction partialValueFunction,
                        String unitOfMeasurement, String source, URI sourceLink) {
    this(criterion, Collections.singletonList(new DataSourceEntry(scale, partialValueFunction, source, sourceLink)), title,
            unitOfMeasurement);
  }

  public CriterionEntry(String criterion, List<DataSourceEntry> dataSources, String title,
                        String unitOfMeasurement) {
    this.criterion = criterion;
    this.dataSources = dataSources;
    this.title = title;
    this.unitOfMeasurement = unitOfMeasurement;
  }

  public String getCriterion() {
    return criterion;
  }

  public List<DataSourceEntry> getDataSources() {
    return dataSources;
  }

  public String getTitle() {
    return title;
  }

  public String getUnitOfMeasurement() {
    return unitOfMeasurement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CriterionEntry that = (CriterionEntry) o;
    return Objects.equals(criterion, that.criterion) &&
        Objects.equals(title, that.title) &&
        Objects.equals(unitOfMeasurement, that.unitOfMeasurement) &&
        Objects.equals(dataSources, that.dataSources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(criterion, title, unitOfMeasurement, dataSources);
  }
}
