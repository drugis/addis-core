package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public abstract class AbstractStudyDataArmValue {

  private final String armInstanceUid;
  private final String armLabel;

  protected AbstractStudyDataArmValue(String armInstanceUid, String armLabel) {
    this.armLabel = armLabel;
    this.armInstanceUid = armInstanceUid;
  }

  public String getArmInstanceUid() {
    return armInstanceUid;
  }

  public String getArmLabel() {
    return armLabel;
  }

}
