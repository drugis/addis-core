package org.drugis.addis.interventions.model;

/**
 * Created by daan on 1-4-16.
 */
public class ConstraintCommand {
  private BoundCommand lowerBound;
  private BoundCommand upperBound;

  public ConstraintCommand() {
  }

  public ConstraintCommand(BoundCommand lowerBound, BoundCommand upperBound) {
    this.lowerBound = lowerBound;
  }

  public BoundCommand getLowerBound() {
    return lowerBound;
  }

  public BoundCommand getUpperBound() {
    return upperBound;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConstraintCommand that = (ConstraintCommand) o;

    if (lowerBound != null ? !lowerBound.equals(that.lowerBound) : that.lowerBound != null) return false;
    return upperBound != null ? upperBound.equals(that.upperBound) : that.upperBound == null;

  }

  @Override
  public int hashCode() {
    int result = lowerBound != null ? lowerBound.hashCode() : 0;
    result = 31 * result + (upperBound != null ? upperBound.hashCode() : 0);
    return result;
  }
}
