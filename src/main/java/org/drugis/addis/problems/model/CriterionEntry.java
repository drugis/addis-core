package org.drugis.addis.problems.model;

import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String criterionUri;
  private String title;
  private List<Double> scale;
  private PartialValueFunction pvf;
  private String uri;

  public CriterionEntry(String criterionUri, String title, List<Double> scale, PartialValueFunction partialValueFunction) {
    this.criterionUri = criterionUri;
    this.title = title;
    this.scale = scale;
    this.pvf = partialValueFunction;
  }

  public String getCriterionUri() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CriterionEntry that = (CriterionEntry) o;

    if (!criterionUri.equals(that.criterionUri)) return false;
    if (pvf != null ? !pvf.equals(that.pvf) : that.pvf != null) return false;
    if (scale != null ? !scale.equals(that.scale) : that.scale != null) return false;
    if (!title.equals(that.title)) return false;
    if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = criterionUri.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (scale != null ? scale.hashCode() : 0);
    result = 31 * result + (pvf != null ? pvf.hashCode() : 0);
    result = 31 * result + (uri != null ? uri.hashCode() : 0);
    return result;
  }
}
