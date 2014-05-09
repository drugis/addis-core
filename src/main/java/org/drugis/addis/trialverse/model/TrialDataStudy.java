package org.drugis.addis.trialverse.model;

import javax.persistence.*;

/**
 * Created by connor on 9-5-14.
 */
@Entity
@Table(name = "studies")
public class TrialDataStudy {

  @Id
  private Long id;

  private String name;

  private Long namespace;

  public TrialDataStudy() {
  }

  public TrialDataStudy(Long id, String name, Long namespace) {
    this.id = id;
    this.name = name;
    this.namespace = namespace;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getNamespace() {
    return namespace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataStudy)) return false;

    TrialDataStudy that = (TrialDataStudy) o;

    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;
    if (!namespace.equals(that.namespace)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + namespace.hashCode();
    return result;
  }
}
