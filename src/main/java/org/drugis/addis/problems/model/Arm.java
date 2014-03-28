package org.drugis.addis.problems.model;

public class Arm {
  Long id;
  Long drugId;
  String name;

  public Arm() {
  }

  public Arm(Long id, Long drugId,  String name) {
    this.id = id;
    this.drugId = drugId;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getDrugId() {
    return drugId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Arm arm = (Arm) o;

    if (!drugId.equals(arm.drugId)) return false;
    if (!id.equals(arm.id)) return false;
    if (!name.equals(arm.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + drugId.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
