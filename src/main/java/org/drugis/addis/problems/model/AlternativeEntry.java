package org.drugis.addis.problems.model;

/**
 * Created by daan on 3/21/14.
 */
public class AlternativeEntry {
  private String interventionUri;
  private String title;

  public AlternativeEntry(String interventionUri, String title) {
    this.interventionUri = interventionUri;
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public String getAlternativeUri() {
    return interventionUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AlternativeEntry that = (AlternativeEntry) o;

    if (!interventionUri.equals(that.interventionUri)) return false;
    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = interventionUri.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }
}
