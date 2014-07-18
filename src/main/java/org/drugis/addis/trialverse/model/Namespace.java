package org.drugis.addis.trialverse.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by connor on 2/12/14.
 */

public class Namespace {
  private String UID;
  private String name;

  private String description;

  public Namespace() {
  }

  public Namespace(String UID, String name, String description) {
    this.UID = UID;
    this.name = name;
    this.description = description;
  }

  public String getUID() {
    return UID;
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

    if (!UID.equals(namespace.UID)) return false;
    if (!description.equals(namespace.description)) return false;
    if (!name.equals(namespace.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = UID.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }
}

