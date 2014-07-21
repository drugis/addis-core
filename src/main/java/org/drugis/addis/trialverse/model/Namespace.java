package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 2/12/14.
 */

public class Namespace {
  private String uid;
  private String name;
  private String description;

  public Namespace() {
  }

  public Namespace(String uid, String name, String description) {
    this.uid = uid;
    this.name = name;
    this.description = description;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Namespace namespace = (Namespace) o;

    if (!uid.equals(namespace.uid)) return false;
    if (!description.equals(namespace.description)) return false;
    if (!name.equals(namespace.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }
}

