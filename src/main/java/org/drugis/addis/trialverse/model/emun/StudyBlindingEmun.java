package org.drugis.addis.trialverse.model.emun;

/**
 * Created by connor on 6-8-14.
 */
public enum StudyBlindingEmun {
  DOUBLE("Double");

  private String label;

  private StudyBlindingEmun(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }

  public static StudyBlindingEmun fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create StudyBlindingEmun enum from empty String");
    } else if (inputString.equalsIgnoreCase("blindingDouble")) {
      return StudyBlindingEmun.DOUBLE;
    }

    throw new IllegalArgumentException("Can not create StudyBlindingEmun enum from given input");
  }

}
