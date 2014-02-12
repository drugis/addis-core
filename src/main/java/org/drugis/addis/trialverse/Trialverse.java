package org.drugis.addis.trialverse;

/**
 * Created by connor on 2/12/14.
 */
public class Trialverse {
  private String name;

  public Trialverse() {
  }

  public Trialverse(String name) {

    this.name = name;
  }

  public String getName() {
    return name;
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

