package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public abstract class AbstractStudyDataArmValue {

  private final String armInstanceUid;
  private final String armLabel;

  private final Integer sampleSize;
  private final String sampleDuration;

  protected AbstractStudyDataArmValue(String armInstanceUid, String armLabel, Integer sampleSize, String sampleDuration) {
    this.armLabel = armLabel;
    this.armInstanceUid = armInstanceUid;
    this.sampleSize = sampleSize;
    this.sampleDuration = sampleDuration;
  }

  public String getArmInstanceUid() {
    return armInstanceUid;
  }

  public String getArmLabel() {
    return armLabel;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public String getSampleDuration() {
    return sampleDuration;
  }

}
