package org.drugis.addis.interventions.model;

import javax.persistence.*;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class TitratedDoseIntervention extends AbstractIntervention {
  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.lowerBoundType" , column = @Column(name="minLowerBoundType") ),
          @AttributeOverride(name="lowerBound.lowerBoundUnit" , column = @Column(name="minLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.lowerBoundValue", column = @Column(name="minLowerBoundValue") ),
          @AttributeOverride(name="upperBound.upperBoundType" , column = @Column(name="minUpperBoundType") ),
          @AttributeOverride(name="upperBound.upperBoundUnit" , column = @Column(name="minUpperBoundUnit") ),
          @AttributeOverride(name="upperBound.upperBoundValue", column = @Column(name="minUpperBoundValue") )
  } )
  private DoseConstraint minConstraint;

  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.lowerBoundType" , column = @Column(name="maxLowerBoundType") ),
          @AttributeOverride(name="lowerBound.lowerBoundUnit" , column = @Column(name="maxLowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.lowerBoundValue", column = @Column(name="maxLowerBoundValue") ),
          @AttributeOverride(name="upperBound.upperBoundType" , column = @Column(name="maxUpperBoundType") ),
          @AttributeOverride(name="upperBound.upperBoundUnit" , column = @Column(name="maxUpperBoundUnit") ),
          @AttributeOverride(name="upperBound.upperBoundValue", column = @Column(name="maxUpperBoundValue") )
  } )
  private DoseConstraint maxConstraint;

  public TitratedDoseIntervention() {
  }

  public TitratedDoseIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
    this.minConstraint = minConstraint;
    this.maxConstraint = maxConstraint;
  }
}
