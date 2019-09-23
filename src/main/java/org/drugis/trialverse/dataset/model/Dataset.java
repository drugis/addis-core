package org.drugis.trialverse.dataset.model;

import org.drugis.addis.security.Account;

import java.util.Date;
import java.util.Objects;

public class Dataset {

  private String uri;
  private Account creator;
  private String title;
  private String description;
  private String headVersion;
  private Boolean archived;
  private String archivedOn;

  public Dataset() {
  }

  public Dataset(String uri, Account creator, String title, String description, String headVersion, Boolean archived, String archivedOn) {
    this.uri = uri;
    this.creator = creator;
    this.title = title;
    this.description = description;
    this.headVersion = headVersion;
    this.archived = archived;
    this.archivedOn = archivedOn;
  }

  public String getUri() {
    return uri;
  }

  public Account getCreator() {
    return creator;
  }

  public String getTitle() {
    return title;
  }

  public Boolean getArchived() {
    return archived;
  }

  public String getDescription() {
    return description;
  }

  public String getHeadVersion() {
    return headVersion;
  }

  public String getArchivedOn() {
    return archivedOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dataset dataset = (Dataset) o;
    return Objects.equals(uri, dataset.uri) &&
            Objects.equals(creator, dataset.creator) &&
            Objects.equals(title, dataset.title) &&
            Objects.equals(description, dataset.description) &&
            Objects.equals(headVersion, dataset.headVersion) &&
            Objects.equals(archived, dataset.archived) &&
            Objects.equals(archivedOn, dataset.archivedOn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, creator, title, description, headVersion, archived, archivedOn);
  }
}
