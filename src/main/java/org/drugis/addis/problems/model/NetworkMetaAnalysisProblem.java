package org.drugis.addis.problems.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 21-5-14.
 */
public class NetworkMetaAnalysisProblem extends AbstractProblem {

  protected List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
  protected List<TreatmentEntry> treatments = new ArrayList<>();
  private Map<String, Map<String, Double>> studyLevelCovariates = new HashMap<>();

  public NetworkMetaAnalysisProblem() {
  }

  public NetworkMetaAnalysisProblem(List<AbstractNetworkMetaAnalysisProblemEntry> entries,
                                    List<TreatmentEntry> treatments, Map<String, Map<String, Double>> studyLevelCovariates) {
    this.entries = entries;
    this.treatments = treatments;
    this.studyLevelCovariates = studyLevelCovariates;
  }

  public List<AbstractNetworkMetaAnalysisProblemEntry> getEntries() {
    return entries;
  }

  public List<TreatmentEntry> getTreatments() {
    return treatments;
  }

  public Map<String, Map<String, Double>> getStudyLevelCovariates() {
    return studyLevelCovariates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) o;

    if (!entries.equals(problem.entries)) return false;
    if (!treatments.equals(problem.treatments)) return false;
    return studyLevelCovariates.equals(problem.studyLevelCovariates);

  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + treatments.hashCode();
    result = 31 * result + studyLevelCovariates.hashCode();
    return result;
  }
}
