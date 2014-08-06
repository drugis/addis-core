package org.drugis.addis.trialverse.model.emun;

/**
 * Created by connor on 6-8-14.
 */
public enum StudyAllocationEnum {

  RANDOMIZED("Randomized");

  private String label;

  private StudyAllocationEnum(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }

  public static StudyAllocationEnum fromString(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      throw new IllegalArgumentException("Can not create StudyAllocationEnum enum from empty String");
    } else if (inputString.equalsIgnoreCase("allocationRandomized")) {
      return StudyAllocationEnum.RANDOMIZED;
    }

    throw new IllegalArgumentException("Can not create StudyAllocationEnum enum from given input");
  }

}
