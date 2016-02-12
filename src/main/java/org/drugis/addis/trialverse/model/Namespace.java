package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 2/12/14.
 */

public class Namespace {
  private String uid;
  private String name;
  private String description;
  private Integer numberOfStudies;
  private String version;
  private Integer ownerId;

  public Namespace() {
  }

  public Namespace(String uid, Integer ownerId, String name, String description, Integer numberOfStudies, String version) {
    this.uid = uid;
    this.ownerId = ownerId;
    this.name = name;
    this.description = description;
    this.numberOfStudies = numberOfStudies;
    this.version = version;
  }

  public String getUid() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getNumberOfStudies() {
    return numberOfStudies;
  }

  public String getVersion() {
    return version;
  }

  public Integer getOwnerId() {
    return ownerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Namespace namespace = (Namespace) o;

    if (!uid.equals(namespace.uid)) return false;
    if (!name.equals(namespace.name)) return false;
    if (description != null ? !description.equals(namespace.description) : namespace.description != null) return false;
    if (numberOfStudies != null ? !numberOfStudies.equals(namespace.numberOfStudies) : namespace.numberOfStudies != null)
      return false;
    if (version != null ? !version.equals(namespace.version) : namespace.version != null) return false;
    return ownerId.equals(namespace.ownerId);

  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (numberOfStudies != null ? numberOfStudies.hashCode() : 0);
    result = 31 * result + (version != null ? version.hashCode() : 0);
    result = 31 * result + ownerId.hashCode();
    return result;
  }
}

