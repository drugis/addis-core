package org.drugis.addis.problems.model;

public class Arm {
  Long id;
  String name;

  public Arm() {
  }

  public Arm(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Arm arm = (Arm) o;

    if (!id.equals(arm.id)) return false;
    if (!name.equals(arm.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
