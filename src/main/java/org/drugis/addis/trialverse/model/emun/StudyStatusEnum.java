package org.drugis.addis.trialverse.model.emun;

/**
 * Created by connor on 6-8-14.
 */
public enum StudyStatusEnum {
  ENROLLING("Enrolling"),
  ACTIVE("Active"),
  COMPLETED("Completed"),
  SUSPENDED("Suspended"),
  TERMINATED("Terminated"),
  WITHDRAWN("Withdrawn");

  private String label;

  StudyStatusEnum(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }

  public static StudyStatusEnum fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create StudyStatus enum from empty String");
    } else if (inputString.equalsIgnoreCase("StatusEnrolling")) {
      return StudyStatusEnum.ENROLLING;
    } else if (inputString.equalsIgnoreCase("StatusActive")) {
      return StudyStatusEnum.ACTIVE;
    } else if (inputString.equalsIgnoreCase("StatusCompleted")) {
      return StudyStatusEnum.COMPLETED;
    } else if (inputString.equalsIgnoreCase("StatusSuspended")) {
      return StudyStatusEnum.SUSPENDED;
    } else if (inputString.equalsIgnoreCase("StatusTerminated")) {
      return StudyStatusEnum.TERMINATED;
    } else if (inputString.equalsIgnoreCase("StatusWithdrawn")) {
      return StudyStatusEnum.WITHDRAWN;
    }

    throw new IllegalArgumentException("Can not create StudyStatus enum from given input " + inputString);
  }
}
