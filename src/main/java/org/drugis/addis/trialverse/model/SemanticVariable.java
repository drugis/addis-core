package org.drugis.addis.trialverse.model;

import java.net.URI;

public class SemanticVariable {
  private URI uri;
  private String label;

  protected SemanticVariable() {
  }

  public SemanticVariable(URI uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SemanticVariable that = (SemanticVariable) o;

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
