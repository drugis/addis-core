package org.drugis.addis.trialverse.model;

import java.net.URI;

/**
 * Created by connor on 3/6/14.
 */
public class SemanticInterventionUriAndName {
  private URI uri;
  private String label;

  public SemanticInterventionUriAndName() {
  }

  public SemanticInterventionUriAndName(URI uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public URI getUri() {
    return uri;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SemanticInterventionUriAndName that = (SemanticInterventionUriAndName) o;

    if (!label.equals(that.label)) return false;
    if (!uri.equals(that.uri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + label.hashCode();
    return result;
  }
}
