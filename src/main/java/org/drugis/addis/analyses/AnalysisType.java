package org.drugis.addis.analyses;

/**
 * Created by connor on 3/11/14.
 */
public enum AnalysisType {
  SINGLE_STUDY_BENEFIT_RISK("Single-study Benefit-Risk");

  public static final String SINGLE_STUDY_BENEFIT_RISK_LABEL = "Single-study Benefit-Risk";

  private String label;

  AnalysisType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static AnalysisType getByLabel(String label) throws Exception {
    switch (label) {
      case SINGLE_STUDY_BENEFIT_RISK_LABEL:
        return SINGLE_STUDY_BENEFIT_RISK;
      default:
        throw new Exception("Attempt to map nonexistent AnalysisType");
    }
  }
}
