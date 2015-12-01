package org.drugis.addis.covariates;

/**
 * Created by connor on 12/1/15.
 */
public class CovariateViewAdepter {

  private String key;
  private String label;
  private String type;

  public CovariateViewAdepter() {
  }

  public CovariateViewAdepter(CovariateOption option) {
    this.key = option.toString();
    this.label = option.getLabel();
    this.type = option.getType().toString();
  }

  public String getKey() {
    return key;
  }

  public String getLabel() {
    return label;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CovariateViewAdepter that = (CovariateViewAdepter) o;

    if (!key.equals(that.key)) return false;
    if (!label.equals(that.label)) return false;
    return type.equals(that.type);

  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + label.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
