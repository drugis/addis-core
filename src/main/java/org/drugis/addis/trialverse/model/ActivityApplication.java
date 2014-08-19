package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 19/08/14.
 */
public class ActivityApplication {

  private String epochUid;
  private String armUid;

  public ActivityApplication(String epochUid, String armUid) {
    this.epochUid = epochUid;
    this.armUid = armUid;
  }

  public String getEpochUid() {
    return epochUid;
  }

  public String getArmUid() {
    return armUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ActivityApplication)) return false;

    ActivityApplication that = (ActivityApplication) o;

    if (!armUid.equals(that.armUid)) return false;
    if (!epochUid.equals(that.epochUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = epochUid.hashCode();
    result = 31 * result + armUid.hashCode();
    return result;
  }
}
