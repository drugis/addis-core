package org.drugis.addis.covariates;

/**
 * Created by connor on 12/1/15.
 */
public enum CovariateOption {
  ALLOCATION_RANDOMIZED(CovariateOptionType.STUDY_CHARACTERISTIC, "Allocation: Randomized", "ontology:study_has_rondomized"),
  BLINDING_AT_LEAST_SINGLE_BLIND(CovariateOptionType.STUDY_CHARACTERISTIC, "Blinding: at least Single Blind", "ontology:study_has_single"),
  BLINDING_AT_LEAST_DOUBLE_BLIND(CovariateOptionType.STUDY_CHARACTERISTIC, "Blinding: at least Double Blind", "ontology:study_has_double"),
  MULTI_CENTER_STUDY(CovariateOptionType.STUDY_CHARACTERISTIC, "Multi-center study", "ontology:study_has_multi-center");

  private CovariateOptionType type;
  private String label;
  private String uri;

  CovariateOption(CovariateOptionType type, String label, String uri) {
    this.type = type;
    this.label = label;
    this.uri = uri;
  }

  public CovariateOptionType getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }
}
