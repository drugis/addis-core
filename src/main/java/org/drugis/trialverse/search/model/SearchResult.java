package org.drugis.trialverse.search.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by connor on 9/4/15.
 */
@JsonDeserialize(using = SearchResultDeserialiser.class)
public class SearchResult {
  private String study;
  private String title;
  private String comment;

  private String owner;
  private String datasetUrl;

  public SearchResult() {
  }

  public SearchResult(String study, String title, String comment) {
    this.study = study;
    this.title = title;
    this.comment = comment;
  }

  public String getStudy() {
    return study;
  }

  public String getTitle() {
    return title;
  }

  public String getComment() {
    return comment;
  }

  public String getOwner() {
    return owner;
  }

  public String getDatasetUrl() {
    return datasetUrl;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setDatasetUrl(String datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchResult that = (SearchResult) o;

    if (!study.equals(that.study)) return false;
    if (!title.equals(that.title)) return false;
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (!owner.equals(that.owner)) return false;
    return datasetUrl.equals(that.datasetUrl);

  }

  @Override
  public int hashCode() {
    int result = study.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + owner.hashCode();
    result = 31 * result + datasetUrl.hashCode();
    return result;
  }
}
