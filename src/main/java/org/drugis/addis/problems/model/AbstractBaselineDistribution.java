package org.drugis.addis.problems.model;

/**
 * Created by daan on 3-3-17.
 */
public class AbstractBaselineDistribution {
  protected String name;
  protected String type;

  public AbstractBaselineDistribution(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public AbstractBaselineDistribution() {
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractBaselineDistribution that = (AbstractBaselineDistribution) o;

    if (!name.equals(that.name)) return false;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
