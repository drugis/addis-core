package org.drugis.addis.interventions.model;

import javax.persistence.*;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class BothDoseTypesIntervention extends AbstractIntervention {
  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="minLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unitName" , column = @Column(name="minLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.unitPeriod" , column = @Column(name="minLowerBoundPeriod") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="minLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="minUpperBoundType") ),
          @AttributeOverride(name="upperBound.unitName" , column = @Column(name="minUpperBoundUnitName") ),
          @AttributeOverride(name="upperBound.unitPeriod" , column = @Column(name="minUpperBoundUnitPeriod") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="minUpperBoundValue") )
  } )
  private DoseConstraint minConstraint;

  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="maxLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unitName" , column = @Column(name="maxLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.unitPeriod" , column = @Column(name="maxLowerBoundPeriod") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="maxLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="maxUpperBoundType") ),
          @AttributeOverride(name="upperBound.unitName" , column = @Column(name="maxUpperBoundUnit") ),
          @AttributeOverride(name="upperBound.unitPeriod" , column = @Column(name="maxUpperBoundUnitPeriod") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="maxUpperBoundValue") )
  } )
  private DoseConstraint maxConstraint;

  public BothDoseTypesIntervention() {
  }

  public BothDoseTypesIntervention(DoseConstraint minConstraint, DoseConstraint maxConstraint) {
    this.minConstraint = minConstraint;
    this.maxConstraint = maxConstraint;
  }

  public BothDoseTypesIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) {
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

    BothDoseTypesIntervention that = (BothDoseTypesIntervention) o;

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
