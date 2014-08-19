package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 19-8-14.
 */
public class FlexibleAdministeredDrug extends AdministeredDrug {

  private Double minValue;
  private String minUnitLabel;
  private String minDosingPeriodicity;

  private Double maxValue;
  private String maxUnitLabel;
  private String maxDosingPeriodicity;

  public FlexibleAdministeredDrug(String drugUid, String drugLabel) {
    super(drugUid, drugLabel);
  }

  public FlexibleAdministeredDrug(String drugUid, String drugLabel, Double minValue, String minUnitLabel, String minDosingPeriodicity, Double maxValue, String maxUnitLabel, String maxDosingPeriodicity) {
    super(drugUid, drugLabel);
    this.minValue = minValue;
    this.minUnitLabel = minUnitLabel;
    this.minDosingPeriodicity = minDosingPeriodicity;
    this.maxValue = maxValue;
    this.maxUnitLabel = maxUnitLabel;
    this.maxDosingPeriodicity = maxDosingPeriodicity;
  }

  private FlexibleAdministeredDrug(FlexibleAdministeredDrugBuilder builder) {
    super(builder.drugUid, builder.drugLabel);

    this.minValue = builder.minValue;
    this.minUnitLabel = builder.minUnitLabel;
    this.minDosingPeriodicity = builder.minDosingPeriodicity;

    this.maxValue = builder.maxValue;
    this.maxUnitLabel = builder.maxUnitLabel;
    this.maxDosingPeriodicity = builder.maxDosingPeriodicity;
  }

  public Double getMinValue() {
    return minValue;
  }

  public String getMinUnitLabel() {
    return minUnitLabel;
  }

  public String getMinDosingPeriodicity() {
    return minDosingPeriodicity;
  }

  public Double getMaxValue() {
    return maxValue;
  }

  public String getMaxUnitLabel() {
    return maxUnitLabel;
  }

  public String getMaxDosingPeriodicity() {
    return maxDosingPeriodicity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FlexibleAdministeredDrug)) return false;
    if (!super.equals(o)) return false;

    FlexibleAdministeredDrug that = (FlexibleAdministeredDrug) o;

    if (maxDosingPeriodicity != null ? !maxDosingPeriodicity.equals(that.maxDosingPeriodicity) : that.maxDosingPeriodicity != null)
      return false;
    if (maxUnitLabel != null ? !maxUnitLabel.equals(that.maxUnitLabel) : that.maxUnitLabel != null) return false;
    if (maxValue != null ? !maxValue.equals(that.maxValue) : that.maxValue != null) return false;
    if (minDosingPeriodicity != null ? !minDosingPeriodicity.equals(that.minDosingPeriodicity) : that.minDosingPeriodicity != null)
      return false;
    if (minUnitLabel != null ? !minUnitLabel.equals(that.minUnitLabel) : that.minUnitLabel != null) return false;
    if (minValue != null ? !minValue.equals(that.minValue) : that.minValue != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (minValue != null ? minValue.hashCode() : 0);
    result = 31 * result + (minUnitLabel != null ? minUnitLabel.hashCode() : 0);
    result = 31 * result + (minDosingPeriodicity != null ? minDosingPeriodicity.hashCode() : 0);
    result = 31 * result + (maxValue != null ? maxValue.hashCode() : 0);
    result = 31 * result + (maxUnitLabel != null ? maxUnitLabel.hashCode() : 0);
    result = 31 * result + (maxDosingPeriodicity != null ? maxDosingPeriodicity.hashCode() : 0);
    return result;
  }

  public static class FlexibleAdministeredDrugBuilder {
    private String drugUid;
    private String drugLabel;

    private Double minValue;
    private String minUnitLabel;
    private String minDosingPeriodicity;

    private Double maxValue;
    private String maxUnitLabel;
    private String maxDosingPeriodicity;

    public FlexibleAdministeredDrug build() {
      return new FlexibleAdministeredDrug(this);
    }

    public FlexibleAdministeredDrugBuilder drugUid(String drugUid) {
      this.drugUid = drugUid;
      return this;
    }

    public FlexibleAdministeredDrugBuilder drugLabel(String drugLabel) {
      this.drugLabel = drugLabel;
      return this;
    }

    public FlexibleAdministeredDrugBuilder minValue(Double minValue) {
      this.minValue = minValue;
      return this;
    }

    public FlexibleAdministeredDrugBuilder minUnitLabel(String minUnitLabel) {
      this.minUnitLabel = minUnitLabel;
      return this;
    }

    public FlexibleAdministeredDrugBuilder minDosingPeriodicity(String minDosingPeriodicity) {
      this.minDosingPeriodicity = minDosingPeriodicity;
      return this;
    }

    public FlexibleAdministeredDrugBuilder maxValue(Double maxValue) {
      this.maxValue = maxValue;
      return this;
    }

    public FlexibleAdministeredDrugBuilder maxUnitLabel(String maxUnitLabel) {
      this.maxUnitLabel = maxUnitLabel;
      return this;
    }

    public FlexibleAdministeredDrugBuilder maxDosingPeriodicity(String maxDosingPeriodicity) {
      this.maxDosingPeriodicity = maxDosingPeriodicity;
      return this;
    }
  }
}
