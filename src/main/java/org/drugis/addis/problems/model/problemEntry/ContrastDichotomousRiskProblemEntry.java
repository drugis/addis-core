package org.drugis.addis.problems.model.problemEntry;

import java.util.Objects;

public class ContrastDichotomousRiskProblemEntry extends AbstractContrastDataEntry {
  private final Double meanDifference;

  public ContrastDichotomousRiskProblemEntry(String studyName, Integer treatmentId, Double meanDifference, Double stdErr) {
    super(studyName, treatmentId, stdErr);
    this.meanDifference = meanDifference;
  }

  public Double getMeanDifference() {
    return meanDifference;
  }

  @Override
  public boolean hasMissingValues() {
    return meanDifference == null ||
            getStandardError() == null ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ContrastDichotomousRiskProblemEntry that = (ContrastDichotomousRiskProblemEntry) o;
    return Objects.equals(meanDifference, that.meanDifference);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), meanDifference);
  }
}
