package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String criterion;
  private String title;
  private String unitOfMeasurement;
  private List<DataSourceEntry> dataSources;

  public CriterionEntry(String criterion, String title) {
    this(criterion, Collections.singletonList(new DataSourceEntry()), title, null);
  }

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

  public List<Double> getScale() {
    return scale;
  }

  public PartialValueFunction getPvf() {
    return pvf;
  }

  public String getUnitOfMeasurement() {
    return unitOfMeasurement;
  }

  public String getSource() {
    return source;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CriterionEntry that = (CriterionEntry) o;

    if (!criterion.equals(that.criterion)) return false;
    if (!scale.equals(that.scale)) return false;
    if (pvf != null ? !pvf.equals(that.pvf) : that.pvf != null) return false;
    if (!title.equals(that.title)) return false;
    if (unitOfMeasurement != null ? !unitOfMeasurement.equals(that.unitOfMeasurement) : that.unitOfMeasurement != null)
      return false;
    if (source != null ? !source.equals(that.source) : that.source != null) return false;
    return sourceLink != null ? sourceLink.equals(that.sourceLink) : that.sourceLink == null;
  }

  @Override
  public int hashCode() {
    int result = criterion.hashCode();
    result = 31 * result + scale.hashCode();
    result = 31 * result + (pvf != null ? pvf.hashCode() : 0);
    result = 31 * result + title.hashCode();
    result = 31 * result + (unitOfMeasurement != null ? unitOfMeasurement.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (sourceLink != null ? sourceLink.hashCode() : 0);
    return result;
  }
}
