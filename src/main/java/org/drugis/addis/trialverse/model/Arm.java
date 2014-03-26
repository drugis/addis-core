package org.drugis.addis.trialverse.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by daan on 3/26/14.
 */
@Entity
@Table(name = "arms")
public class Arm {
  @Id
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
