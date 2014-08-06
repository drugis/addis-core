package org.drugis.addis.trialverse.model.emun;

/**
 * Created by connor on 6-8-14.
 */
public enum StudyStatusEnum {
  UNKNOWN("Unknown"), COMPLETED("Completed");

  private String label;

  private StudyStatusEnum(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }

  public static StudyStatusEnum fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create StudyStatus enum from empty String");
    } else if (inputString.equalsIgnoreCase("statusUnknown")) {
      return StudyStatusEnum.UNKNOWN;
    } else if (inputString.equalsIgnoreCase("statusCompleted")) {
      return StudyStatusEnum.COMPLETED;
    }

    throw new IllegalArgumentException("Can not create StudyStatus enum from given input");
  }
}
