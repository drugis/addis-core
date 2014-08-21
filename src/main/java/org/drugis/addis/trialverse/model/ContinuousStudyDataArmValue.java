package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public class ContinuousStudyDataArmValue extends AbstractStudyDataArmValue {

  private Double mean;
  private Double std;

  private ContinuousStudyDataArmValue(ContinuousStudyDataArmValueBuilder builder) {
    super(builder.armInstanceUid,
            builder.armLabel,
            builder.sampleSize,
            builder.sampleDuration);
    this.mean = builder.mean;
    this.std = builder.std;
  }

  public Double getMean() {
    return mean;
  }

  public Double getStd() {
    return std;
  }

  public static class ContinuousStudyDataArmValueBuilder {

    private final String armInstanceUid;
    private final String armLabel;

    private Double mean;
    private Double std;

    private Integer sampleSize;
    private String sampleDuration;

    public ContinuousStudyDataArmValueBuilder(String armInstanceUid, String armLabel) {
      this.armInstanceUid = armInstanceUid;
      this.armLabel = armLabel;
    }

    public ContinuousStudyDataArmValue build() {
      return new ContinuousStudyDataArmValue(this);
    }

    public ContinuousStudyDataArmValueBuilder mean(Double mean) {
      this.mean = mean;
      return this;
    }

    public ContinuousStudyDataArmValueBuilder std(Double std) {
      this.std = std;
      return this;
    }

    public ContinuousStudyDataArmValueBuilder sampleSize(Integer sampleSize) {
      this.sampleSize = sampleSize;
      return this;
    }

    public ContinuousStudyDataArmValueBuilder sampleDuration(String sampleDuration) {
      this.sampleDuration = sampleDuration;
      return this;
    }
  }
}
