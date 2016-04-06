package org.drugis.addis.interventions.model;

import javax.persistence.*;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class TitratedDoseIntervention extends AbstractIntervention {
  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="minLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unit" , column = @Column(name="minLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="minLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="minUpperBoundType") ),
          @AttributeOverride(name="upperBound.unit" , column = @Column(name="minUpperBoundUnit") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="minUpperBoundValue") )
  } )
  private DoseConstraint minConstraint;

  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="maxLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unit" , column = @Column(name="maxLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="maxLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="maxUpperBoundType") ),
          @AttributeOverride(name="upperBound.unit" , column = @Column(name="maxUpperBoundUnit") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="maxUpperBoundValue") )
  } )
  private DoseConstraint maxConstraint;

  public TitratedDoseIntervention() {
  }

  public TitratedDoseIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
    this.minConstraint = minConstraint;
    this.maxConstraint = maxConstraint;
  }

  public DoseConstraint getMinConstraint() {
    return minConstraint;
  }

  public DoseConstraint getMaxConstraint() {
    return maxConstraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TitratedDoseIntervention that = (TitratedDoseIntervention) o;

    if (minConstraint != null ? !minConstraint.equals(that.minConstraint) : that.minConstraint != null) return false;
    return maxConstraint != null ? maxConstraint.equals(that.maxConstraint) : that.maxConstraint == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (minConstraint != null ? minConstraint.hashCode() : 0);
    result = 31 * result + (maxConstraint != null ? maxConstraint.hashCode() : 0);
    return result;
  }
}
