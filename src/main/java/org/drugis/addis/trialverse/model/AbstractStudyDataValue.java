package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 21-8-14.
 */
public abstract class AbstractStudyDataValue {

  private final String instanceUid;
  private final String label;
  private final Boolean isArm;

  protected AbstractStudyDataValue(String instanceUid, String label, Boolean isArm) {
    this.label = label;
    this.instanceUid = instanceUid;
    this.isArm = isArm;
  }

  public String getInstanceUid() {
    return instanceUid;
  }

  public String getLabel() {
    return label;
  }

  public Boolean isArm() {
    return isArm;
  }
}
