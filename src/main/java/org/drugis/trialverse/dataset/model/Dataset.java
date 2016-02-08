package org.drugis.trialverse.dataset.model;

import org.drugis.addis.security.Account;

/**
 * Created by connor on 6-11-14.
 */
public class Dataset {

  private String uri;
  private Account creator;
  private String title;
  private String description;
  private String headVersion;

  public Dataset() {
  }

  public Dataset(String uri, Account creator, String title, String description, String headVersion) {
    this.uri = uri;
    this.creator = creator;
    this.title = title;
    this.description = description;
    this.headVersion = headVersion;
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

  public String getDescription() {
    return description;
  }

  public String getHeadVersion() {
    return headVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Dataset dataset = (Dataset) o;

    if (!uri.equals(dataset.uri)) return false;
    if (!creator.equals(dataset.creator)) return false;
    if (!title.equals(dataset.title)) return false;
    if (description != null ? !description.equals(dataset.description) : dataset.description != null) return false;
    return headVersion.equals(dataset.headVersion);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + headVersion.hashCode();
    return result;
  }
}
