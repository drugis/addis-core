package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 14-8-14.
 */
public class TreatmentActivity {
  private String treatmentActivityUri;
  private String epochUri;
  private String epochLabel;
  private String treatmentActivityTypeLabel;
  private String armLabel;
  private String treatmentDrugLabel;
  private Double minValue;
  private String minUnitLabel;
  private String minDosingPeriodicity;
  private Double maxValue;
  private String maxUnitLabel;
  private String maxDosingPeriodicity;
  private Double fixedValue;
  private String fixedUnitLabel;
  private String fixedDosingPeriodicity;

  public TreatmentActivity() {
  }

  private TreatmentActivity(StudyDesignBuilder builder) {
    this.treatmentActivityUri = builder.treatmentActivityUri;
    this.epochUri = builder.epochUri;
    this.epochLabel = builder.epochLabel;
    this.treatmentActivityTypeLabel = builder.treatmentActivityTypeLabel;
    this.armLabel = builder.armLabel;
    this.treatmentDrugLabel = builder.treatmentDrugLabel;

    this.minValue = builder.minValue;
    this.minUnitLabel = builder.minUnitLabel;
    this.minDosingPeriodicity = builder.minDosingPeriodicity;

    this.maxValue = builder.maxValue;
    this.maxUnitLabel = builder.maxUnitLabel;
    this.maxDosingPeriodicity = builder.maxDosingPeriodicity;

    this.fixedValue = builder.fixedValue;
    this.fixedUnitLabel = builder.fixedUnitLabel;
    this.fixedDosingPeriodicity = builder.fixedDosingPeriodicity;
  }

  public static class StudyDesignBuilder {
    private String treatmentActivityUri;
    private String epochUri;
    private String epochLabel;
    private String treatmentActivityTypeLabel;
    private String armLabel;
    private String treatmentDrugLabel;

    private Double minValue;
    private String minUnitLabel;
    private String minDosingPeriodicity;

    private Double maxValue;
    private String maxUnitLabel;
    private String maxDosingPeriodicity;

    private Double fixedValue;
    private String fixedUnitLabel;
    private String fixedDosingPeriodicity;

    public TreatmentActivity build() {
      return new TreatmentActivity(this);
    }

    public StudyDesignBuilder treatmentActivityUri(String treatmentActivityUri) {
      this.treatmentActivityUri = treatmentActivityUri;
      return this;
    }

    public StudyDesignBuilder epochUri(String epochUri) {
      this.epochUri = epochUri;
      return this;
    }

    public StudyDesignBuilder epochLabel(String epochLabel) {
      this.epochLabel = epochLabel;
      return this;
    }

    public StudyDesignBuilder treatmentActivityTypeLabel(String treatmentActivityTypeLabel) {
      this.treatmentActivityTypeLabel = treatmentActivityTypeLabel;
      return this;
    }

    public StudyDesignBuilder armLabel(String armLabel) {
      this.armLabel = armLabel;
      return this;
    }

    public StudyDesignBuilder treatmentDrugLabel(String treatmentDrugLabel) {
      this.treatmentDrugLabel = treatmentDrugLabel;
      return this;
    }

    public StudyDesignBuilder minValue(Double minValue) {
      this.minValue = minValue;
      return this;
    }

    public StudyDesignBuilder minUnitLabel(String minUnitLabel) {
      this.minUnitLabel = minUnitLabel;
      return this;
    }

    public StudyDesignBuilder minDosingPeriodicity(String minDosingPeriodicity) {
      this.minDosingPeriodicity = minDosingPeriodicity;
      return this;
    }

    public StudyDesignBuilder maxValue(Double maxValue) {
      this.maxValue = maxValue;
      return this;
    }

    public StudyDesignBuilder maxUnitLabel(String maxUnitLabel) {
      this.maxUnitLabel = maxUnitLabel;
      return this;
    }

    public StudyDesignBuilder maxDosingPeriodicity(String maxDosingPeriodicity) {
      this.maxDosingPeriodicity = maxDosingPeriodicity;
      return this;
    }

    public StudyDesignBuilder fixedValue(Double fixedValue) {
      this.fixedValue = fixedValue;
      return this;
    }

    public StudyDesignBuilder fixedUnitLabel(String fixedUnitLabel) {
      this.fixedUnitLabel = fixedUnitLabel;
      return this;
    }

    public StudyDesignBuilder fixedDosingPeriodicity(String fixedDosingPeriodicity) {
      this.fixedDosingPeriodicity = fixedDosingPeriodicity;
      return this;
    }

  }

  public String getTreatmentActivityUri() {
    return treatmentActivityUri;
  }

  public String getEpochUri() {
    return epochUri;
  }

  public String getEpochLabel() {
    return epochLabel;
  }

  public String getTreatmentActivityTypeLabel() {
    return treatmentActivityTypeLabel;
  }

  public String getArmLabel() {
    return armLabel;
  }

  public String getTreatmentDrugLabel() {
    return treatmentDrugLabel;
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

  public Double getFixedValue() {
    return fixedValue;
  }

  public String getFixedUnitLabel() {
    return fixedUnitLabel;
  }

  public String getFixedDosingPeriodicity() {
    return fixedDosingPeriodicity;
  }
}
