package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private URI criterionUri;
  private List<Double> scale;
  private PartialValueFunction pvf;
  private String title;
  private String unitOfMeasurement;

  public CriterionEntry(URI criterionUri, String title) {
    this(criterionUri, title, null, null, null);
  }

  public CriterionEntry(URI criterionUri, String title, List<Double> scale, PartialValueFunction partialValueFunction, String unitOfMeasurement) {
    this.criterionUri = criterionUri;
    this.title = title;
    this.scale = scale;
    this.pvf = partialValueFunction;
    this.unitOfMeasurement = unitOfMeasurement;
  }

  public URI getCriterionUri() {
    return criterionUri;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CriterionEntry that = (CriterionEntry) o;

    if (!criterionUri.equals(that.criterionUri)) return false;
    if (!scale.equals(that.scale)) return false;
    if (pvf != null ? !pvf.equals(that.pvf) : that.pvf != null) return false;
    if (!title.equals(that.title)) return false;
    return unitOfMeasurement != null ? unitOfMeasurement.equals(that.unitOfMeasurement) : that.unitOfMeasurement == null;

  }

  @Override
  public int hashCode() {
    int result = criterionUri.hashCode();
    result = 31 * result + scale.hashCode();
    result = 31 * result + (pvf != null ? pvf.hashCode() : 0);
    result = 31 * result + title.hashCode();
    result = 31 * result + (unitOfMeasurement != null ? unitOfMeasurement.hashCode() : 0);
    return result;
  }
}
