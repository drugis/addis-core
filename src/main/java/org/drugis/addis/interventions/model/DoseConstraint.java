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

  public DoseConstraint(LowerBoundCommand lowerBound, UpperBoundCommand upperBound) {
    this.lowerBound = new LowerDoseBound(lowerBound.getType(), lowerBound.getValue(), lowerBound.getUnit());
    this.upperBound = new UpperDoseBound(upperBound.getType(), upperBound.getValue(), upperBound.getUnit());
  }

}
