package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 17-10-14.
 */
public class StudyTreatmentArm {
  private String armUid;
  private Set<String> interventionUids = new HashSet<>();

  public StudyTreatmentArm(String armUid) {
    this.armUid = armUid;
  }

  public String getArmUid() {
    return armUid;
  }

  public Set<String> getInterventionUids() {
    return interventionUids;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StudyTreatmentArm that = (StudyTreatmentArm) o;

    if (!armUid.equals(that.armUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return armUid.hashCode();
  }
}
