package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public class ContinuousStudyDataValue extends AbstractStudyDataValue {

  private Integer sampleSize;
  private String sampleDuration;
  private Double mean;
  private Double std;

  private ContinuousStudyDataValue(ContinuousStudyDataValueBuilder builder) {
    super(builder.instanceUid, builder.label, builder.isArm);
    this.sampleSize = builder.sampleSize;
    this.sampleDuration = builder.sampleDuration;
    this.mean = builder.mean;
    this.std = builder.std;

  }

  public Double getMean() {
    return mean;
  }

  public Double getStd() {
    return std;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public String getSampleDuration() {
    return sampleDuration;
  }

  public static class ContinuousStudyDataValueBuilder {

    private final String instanceUid;
    private final String label;
    private final Boolean isArm;

    private Double mean;
    private Double std;

    private Integer sampleSize;
    private String sampleDuration;

    public ContinuousStudyDataValueBuilder(String instanceUid, String label, Boolean isArm) {
      this.instanceUid = instanceUid;
      this.label = label;
      this.isArm = isArm;
    }

    public ContinuousStudyDataValue build() {
      return new ContinuousStudyDataValue(this);
    }

    public ContinuousStudyDataValueBuilder mean(Double mean) {
      this.mean = mean;
      return this;
    }

    public ContinuousStudyDataValueBuilder std(Double std) {
      this.std = std;
      return this;
    }

    public ContinuousStudyDataValueBuilder sampleSize(Integer sampleSize) {
      this.sampleSize = sampleSize;
      return this;
    }

    public ContinuousStudyDataValueBuilder sampleDuration(String sampleDuration) {
      this.sampleDuration = sampleDuration;
      return this;
    }
  }
}
