package org.drugis.addis.trialverse.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by daan on 3/19/14.
 */
@Entity
@Table(name = "studies")
public class Study {

  @Id
  private Long id;
  private String name;
  private String title;

  public Study() {
  }

  public Study(Long id, String name, String title) {
    this.id = id;
    this.name = name;
    this.title = title;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Study study = (Study) o;

    if (!id.equals(study.id)) return false;
    if (!name.equals(study.name)) return false;
    if (!title.equals(study.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }
}
