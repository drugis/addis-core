package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 2/28/14.
 */
public class SemanticOutcome {
  private String uri;
  private String label;

  protected SemanticOutcome() {
  }

  public SemanticOutcome(String uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
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

    SemanticOutcome that = (SemanticOutcome) o;

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
