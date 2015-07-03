package org.drugis.addis.problems.model;

import org.drugis.addis.interventions.Intervention;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 21-5-14.
 */
public class NetworkMetaAnalysisProblem extends AbstractProblem {

  protected List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
  protected List<TreatmentEntry> treatments = new ArrayList<>();

  public NetworkMetaAnalysisProblem() {
  }

  public NetworkMetaAnalysisProblem(List<AbstractNetworkMetaAnalysisProblemEntry> entries, List<TreatmentEntry> treatments) {
    this.entries = entries;
    this.treatments = treatments;
  }

  public List<AbstractNetworkMetaAnalysisProblemEntry> getEntries() {
    return entries;
  }

  public List<TreatmentEntry> getTreatments() {
    return treatments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkMetaAnalysisProblem that = (NetworkMetaAnalysisProblem) o;

    if (!entries.equals(that.entries)) return false;
    if (!treatments.equals(that.treatments)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + treatments.hashCode();
    return result;
  }
}
