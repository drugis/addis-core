package org.drugis.addis.problems.model;

import java.util.Objects;

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
    return Objects.equals(id, that.id) &&
            Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, name);
  }
}
