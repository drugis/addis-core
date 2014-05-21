package org.drugis.addis.problems.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 21-5-14.
 */
public class NetworkMetaAnalysisProblem extends AbstractProblem {

  private List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

  public NetworkMetaAnalysisProblem() {
  }

  public NetworkMetaAnalysisProblem(List<AbstractNetworkMetaAnalysisProblemEntry> entries) {
    this.entries = entries != null ? entries : new ArrayList<AbstractNetworkMetaAnalysisProblemEntry>() ;
  }

  public List<AbstractNetworkMetaAnalysisProblemEntry> getEntries() {
    return entries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkMetaAnalysisProblem that = (NetworkMetaAnalysisProblem) o;

    if (!entries.equals(that.entries)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return entries.hashCode();
  }
}
