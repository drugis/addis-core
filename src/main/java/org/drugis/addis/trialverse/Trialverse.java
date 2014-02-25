package org.drugis.addis.trialverse;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by connor on 2/12/14.
 */
@Embeddable
public class Trialverse {
  @Column(name = "trialverse")
  private String name;

  public Trialverse() {
  }

  public Trialverse(String name) {

    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Trialverse that = (Trialverse) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}

