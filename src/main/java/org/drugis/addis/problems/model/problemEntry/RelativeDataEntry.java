package org.drugis.addis.problems.model.problemEntry;

import java.util.List;
import java.util.Objects;

public class RelativeDataEntry {
  private final BaseArm baseArm;
  private final List<AbstractProblemEntry> otherArms;

  public RelativeDataEntry(Integer baseArmTreatment, Double baseArmStandardError, List<AbstractProblemEntry> otherArms) {
    this.baseArm = new BaseArm(baseArmTreatment, baseArmStandardError);
    this.otherArms = otherArms;
  }

  public BaseArm getBaseArm() {
    return baseArm;
  }

  public List<AbstractProblemEntry> getOtherArms() {
    return otherArms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RelativeDataEntry that = (RelativeDataEntry) o;
    return Objects.equals(baseArm, that.baseArm) &&
            Objects.equals(otherArms, that.otherArms);
  }

  @Override
  public int hashCode() {

    return Objects.hash(baseArm, otherArms);
  }

  protected class BaseArm {
    private Integer treatment;
    private Double baseArmStandardError;

    BaseArm(Integer treatment, Double baseArmStandardError) {
      this.treatment = treatment;
      this.baseArmStandardError = baseArmStandardError;
    }

    public Integer getTreatment() {
      return treatment;
    }

    public Double getBaseArmStandardError() {
      return baseArmStandardError;
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      BaseArm baseArm = (BaseArm) o;
      return Objects.equals(treatment, baseArm.treatment) &&
              Objects.equals(baseArmStandardError, baseArm.baseArmStandardError);
    }

    @Override
    public int hashCode() {

      return Objects.hash(treatment, baseArmStandardError);
    }
  }
}
