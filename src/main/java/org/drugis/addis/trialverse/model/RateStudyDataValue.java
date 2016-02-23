package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public class RateStudyDataValue extends AbstractStudyDataValue {

  private Integer sampleSize;
  private String sampleDuration;
  private Long count;

  private RateStudyDataValue(RateStudyDataValueBuilder builder) {
    super(builder.instanceUid, builder.label, builder.isArm);
    this.sampleSize = builder.sampleSize;
    this.sampleDuration = builder.sampleDuration;
    this.count = builder.count;
  }

  public Long getCount() {
    return count;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public String getSampleDuration() {
    return sampleDuration;
  }

  public static class RateStudyDataValueBuilder {

    private final String instanceUid;
    private final String label;
    private final Boolean isArm;
    private Long count;
    private Integer sampleSize;
    private String sampleDuration;

    public RateStudyDataValueBuilder(String instanceUid, String label, Boolean isArm) {
      this.instanceUid = instanceUid;
      this.label = label;
      this.isArm = isArm;
    }

    public RateStudyDataValue build() {
      return new RateStudyDataValue(this);
    }

    public RateStudyDataValueBuilder count(Long count) {
      this.count = count;
      return this;
    }

    public RateStudyDataValueBuilder sampleSize(Integer sampleSize) {
      this.sampleSize = sampleSize;
      return this;
    }

    public RateStudyDataValueBuilder sampleDuration(String sampleDuration) {
      this.sampleDuration = sampleDuration;
      return this;
    }

  }
}
