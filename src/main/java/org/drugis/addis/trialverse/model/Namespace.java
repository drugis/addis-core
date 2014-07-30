package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 2/12/14.
 */

public class Namespace {
  private String uid;
  private String name;
  private String description;
  private Integer numberOfStudies;
  private String sourceUrl;

  public Namespace() {
  }

  public Namespace(String uid, String name, String description, Integer numberOfStudies, String sourceUrl) {
    this.uid = uid;
    this.name = name;
    this.description = description;
    this.numberOfStudies = numberOfStudies;
    this.sourceUrl = sourceUrl;
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

  public String getSourceUrl() {
    return sourceUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Namespace namespace = (Namespace) o;

    if (!description.equals(namespace.description)) return false;
    if (!name.equals(namespace.name)) return false;
    if (!numberOfStudies.equals(namespace.numberOfStudies)) return false;
    if (!sourceUrl.equals(namespace.sourceUrl)) return false;
    if (!uid.equals(namespace.uid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + numberOfStudies.hashCode();
    result = 31 * result + sourceUrl.hashCode();
    return result;
  }
}

