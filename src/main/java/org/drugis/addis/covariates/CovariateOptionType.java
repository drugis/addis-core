package org.drugis.addis.covariates;

/**
 * Created by connor on 12/1/15.
 */
public enum CovariateOptionType {
  STUDY_CHARACTERISTIC("Study Characteristic"), POPULATION_CHARACTERISTIC("Population Characteristic");

  private String label;

  CovariateOptionType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
