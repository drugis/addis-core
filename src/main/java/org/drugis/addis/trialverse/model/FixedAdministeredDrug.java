package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 19-8-14.
 */
public class FixedAdministeredDrug extends AdministeredDrug {

  private Double fixedValue;
  private String fixedUnitLabel;
  private String fixedDosingPeriodicity;

  public FixedAdministeredDrug(String drugUid, String drugLabel) {
    super(drugUid, drugLabel);
  }

  private FixedAdministeredDrug(FixedAdministeredDrugBuilder builder) {
    super(builder.drugUid, builder.drugLabel);

    this.fixedValue = builder.fixedValue;
    this.fixedUnitLabel = builder.fixedUnitLabel;
    this.fixedDosingPeriodicity = builder.fixedDosingPeriodicity;

    this.fixedValue = builder.fixedValue;
    this.fixedUnitLabel = builder.fixedUnitLabel;
    this.fixedDosingPeriodicity = builder.fixedDosingPeriodicity;
  }

  public Double getFixedValue() {
    return fixedValue;
  }

  public String getFixedUnitLabel() {
    return fixedUnitLabel;
  }

  public String getFixedDosingPeriodicity() {
    return fixedDosingPeriodicity;
  }

  public static class FixedAdministeredDrugBuilder {
    private String drugUid;
    private String drugLabel;

    private Double fixedValue;
    private String fixedUnitLabel;
    private String fixedDosingPeriodicity;

    public FixedAdministeredDrug build() {
      return new FixedAdministeredDrug(this);
    }

    public FixedAdministeredDrugBuilder drugUid(String drugUid) {
      this.drugUid = drugUid;
      return this;
    }

    public FixedAdministeredDrugBuilder drugLabel(String drugLabel) {
      this.drugLabel = drugLabel;
      return this;
    }

    public FixedAdministeredDrugBuilder fixedValue(Double fixedValue) {
      this.fixedValue = fixedValue;
      return this;
    }

    public FixedAdministeredDrugBuilder fixedUnitLabel(String fixedUnitLabel) {
      this.fixedUnitLabel = fixedUnitLabel;
      return this;
    }

    public FixedAdministeredDrugBuilder fixedDosingPeriodicity(String fixedDosingPeriodicity) {
      this.fixedDosingPeriodicity = fixedDosingPeriodicity;
      return this;
    }

  }
}
