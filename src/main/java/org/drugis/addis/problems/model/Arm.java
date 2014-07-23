package org.drugis.addis.problems.model;

public class Arm {
  String uid;
  String drugUid;
  String name;

  public Arm() {
  }

  public Arm(String uid, String drugUid, String name) {
    this.uid = uid;
    this.drugUid = drugUid;
    this.name = name;
  }

  public String getUid() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public String getDrugUid() {
    return drugUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Arm arm = (Arm) o;

    if (!drugUid.equals(arm.drugUid)) return false;
    if (!uid.equals(arm.uid)) return false;
    if (!name.equals(arm.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + drugUid.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
