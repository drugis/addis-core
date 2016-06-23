package org.drugis.addis.problems.model;

import java.net.URI;

public class Arm {
  URI uri;
  String drugUid;
  String name;

  public Arm() {
  }

  public Arm(URI uri, String drugUid, String name) {
    this.uri = uri;
    this.drugUid = drugUid;
    this.name = name;
  }

  public URI getUri() {
    return uri;
  }

  public String getDrugUid() {
    return drugUid;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Arm arm = (Arm) o;

    if (!uri.equals(arm.uri)) return false;
    if (!drugUid.equals(arm.drugUid)) return false;
    return name.equals(arm.name);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + drugUid.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
