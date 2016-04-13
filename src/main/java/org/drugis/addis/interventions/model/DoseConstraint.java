package org.drugis.addis.interventions.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * Created by daan on 5-4-16.
 */
@Embeddable
public class DoseConstraint {
  @Embedded
  private LowerDoseBound lowerBound;
  @Embedded
  private UpperDoseBound upperBound;

  public DoseConstraint() {
  }

  public LowerDoseBound getLowerBound() {
    return lowerBound;
  }

  public UpperDoseBound getUpperBound() {
    return upperBound;
  }

  public DoseConstraint(LowerBoundCommand lowerBound, UpperBoundCommand upperBound) throws InvalidConstraintException {

    if(lowerBound == null && upperBound == null) {
      throw new InvalidConstraintException("upper and lower bounds may not both be null");
    }

    if(lowerBound != null) {
      this.lowerBound = new LowerDoseBound(lowerBound.getType(), lowerBound.getValue(), lowerBound.getUnitName(), lowerBound.getUnitPeriod(), lowerBound.getUnitConcept());
    }
    if(upperBound != null) {
      this.upperBound = new UpperDoseBound(upperBound.getType(), upperBound.getValue(), upperBound.getUnitName(), upperBound.getUnitPeriod(), upperBound.getUnitConcept());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DoseConstraint that = (DoseConstraint) o;

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
