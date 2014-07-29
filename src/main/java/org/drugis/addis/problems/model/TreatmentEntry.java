package org.drugis.addis.problems.model;

/**
 * Created by daan on 9-7-14.
 */
public class TreatmentEntry {
  private Integer id;
  private String name;

  public TreatmentEntry() {
  }

  public TreatmentEntry(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TreatmentEntry that = (TreatmentEntry) o;

    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
