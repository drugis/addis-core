package org.drugis.trialverse.dataset.model;

import org.drugis.trialverse.security.Account;

/**
 * Created by connor on 6-11-14.
 */
public class Dataset {

  private String Uid;
  private Account creator;
  private String title;
  private String description;

  public Dataset() {
  }

  public Dataset(String uid, Account creator, String title, String description) {
    Uid = uid;
    this.creator = creator;
    this.title = title;
    this.description = description;
  }

  public String getUid() {
    return Uid;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Dataset dataset = (Dataset) o;

    if (!Uid.equals(dataset.Uid)) return false;
    if (!creator.equals(dataset.creator)) return false;
    if (description != null ? !description.equals(dataset.description) : dataset.description != null) return false;
    if (!title.equals(dataset.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = Uid.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}
