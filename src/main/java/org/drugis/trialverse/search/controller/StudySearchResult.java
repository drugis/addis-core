package org.drugis.trialverse.search.controller;

/**
 * Created by connor on 01/09/15.
 */
public class StudySearchResult {
  private String title;
  private String description;

  private String graphUri;

  public StudySearchResult(String title, String description, String graphUri) {
    this.title = title;
    this.description = description;
    this.graphUri = graphUri;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getGraphUri() {
    return graphUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StudySearchResult)) return false;

    StudySearchResult that = (StudySearchResult) o;

    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (!graphUri.equals(that.graphUri)) return false;
    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + graphUri.hashCode();
    return result;
  }
}
