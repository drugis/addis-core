package org.drugis.addis.trialverse;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by connor on 2/12/14.
 */
@Entity
@Table(name = "namespaces")
public class Trialverse {
  @Id
  private Long id;

  private String name;

  private String description;

  public Trialverse() {
  }

  public Trialverse(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Trialverse that = (Trialverse) o;

    if (!description.equals(that.description)) return false;
    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }
}

