package org.drugis.addis.trialverse.model;

import java.net.URI;
import java.util.Objects;

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
    return Objects.equals(uri, that.uri) &&
            Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {

    return Objects.hash(uri, label);
  }
}
