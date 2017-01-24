package org.drugis.addis.statistics.model;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 20-1-17.
 */
public class Estimates {
  private URI baselineUri;
  private Map<URI, List<Estimate>> estimates = new HashMap<>();

  public Estimates() {
  }

  public Estimates(URI baselineUri, Map<URI, List<Estimate>> estimates) {
    this.baselineUri = baselineUri;
    this.estimates = estimates;
  }

  public URI getBaselineUri() {
    return baselineUri;
  }

  public Map<URI, List<Estimate>> getEstimates() {
    return estimates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Estimates estimates1 = (Estimates) o;

    if (baselineUri != null ? !baselineUri.equals(estimates1.baselineUri) : estimates1.baselineUri != null)
      return false;
    return estimates != null ? estimates.equals(estimates1.estimates) : estimates1.estimates == null;
  }

  @Override
  public int hashCode() {
    int result = baselineUri != null ? baselineUri.hashCode() : 0;
    result = 31 * result + (estimates != null ? estimates.hashCode() : 0);
    return result;
  }
}
