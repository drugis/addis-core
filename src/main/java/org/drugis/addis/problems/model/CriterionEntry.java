package org.drugis.addis.problems.model;

import java.util.List;
import java.util.Objects;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String title;
  private String unitOfMeasurement;
  private List<DataSourceEntry> dataSources;

  public CriterionEntry(List<DataSourceEntry> dataSources, String title,
                        String unitOfMeasurement) {
    this.dataSources = dataSources;
    this.title = title;
    this.unitOfMeasurement = unitOfMeasurement;
  }

  public CriterionEntry(List<DataSourceEntry> dataSources, String title) {
    this(dataSources, title, null);
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
    return Objects.equals(title, that.title) &&
        Objects.equals(unitOfMeasurement, that.unitOfMeasurement) &&
        Objects.equals(dataSources, that.dataSources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, unitOfMeasurement, dataSources);
  }
}
