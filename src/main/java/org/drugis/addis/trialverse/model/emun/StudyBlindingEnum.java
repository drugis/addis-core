package org.drugis.addis.trialverse.model.emun;

/**
 * Created by connor on 6-8-14.
 */
public enum StudyBlindingEnum {
  OPEN("Open"),
  SINGLE("Single"),
  DOUBLE("Double"),
  TRIPLE("Triple");

  private String label;

  private StudyBlindingEnum(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }

  public static StudyBlindingEnum fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create StudyBlindingEnum enum from empty String");
    } else if (inputString.equalsIgnoreCase("OpenLabel")) {
      return StudyBlindingEnum.OPEN;
    } else if (inputString.equalsIgnoreCase("SingleBlind")) {
      return StudyBlindingEnum.SINGLE;
    } else if (inputString.equalsIgnoreCase("DoubleBlind")) {
      return StudyBlindingEnum.DOUBLE;
    } else if (inputString.equalsIgnoreCase("TripleBlind")) {
      return StudyBlindingEnum.TRIPLE;
    }
    throw new IllegalArgumentException("Can not create StudyBlindingEnum enum from given input " + inputString);

  }
}
