package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public class RateStudyDataArmValue extends AbstractStudyDataArmValue {

  private Long count;

  private RateStudyDataArmValue(RateStudyDataArmValueBuilder builder) {
    super(builder.armInstanceUid,
            builder.armLabel,
            builder.sampleSize,
            builder.sampleDuration);
    this.count = builder.count;
  }

  public Long getCount() {
    return count;
  }

  public static class RateStudyDataArmValueBuilder {

    private final String armInstanceUid;
    private final String armLabel;
    private Long count;
    private Integer sampleSize;
    private String sampleDuration;

    public RateStudyDataArmValueBuilder(String armInstanceUid, String armLabel) {
      this.armInstanceUid = armInstanceUid;
      this.armLabel = armLabel;
    }

    public RateStudyDataArmValue build() {
      return new RateStudyDataArmValue(this);
    }

    public RateStudyDataArmValueBuilder count(Long count) {
      this.count = count;
      return this;
    }

    public RateStudyDataArmValueBuilder sampleSize(Integer sampleSize) {
      this.sampleSize = sampleSize;
      return this;
    }

    public RateStudyDataArmValueBuilder sampleDuration(String sampleDuration) {
      this.sampleDuration = sampleDuration;
      return this;
    }

  }
}
