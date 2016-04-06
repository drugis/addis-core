package org.drugis.addis.interventions.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class FixedDoseIntervention extends AbstractIntervention implements Serializable {
  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="lowerBoundType") ),
          @AttributeOverride(name="lowerBound.unit" , column = @Column(name="lowerBoundUnit") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="lowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="upperBoundType") ),
          @AttributeOverride(name="upperBound.unit" , column = @Column(name="upperBoundUnit") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="upperBoundValue") )
  } )
  DoseConstraint constraint;

  public FixedDoseIntervention() {
  }

  public FixedDoseIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionLabel,
                               String semanticInterventionUri, DoseConstraint constraint) {
    super(id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri);
    this.constraint = constraint;
  }

  public FixedDoseIntervention(Integer project, String name, String motivation, String semanticInterventionLabel,
                               String semanticInterventionUri, DoseConstraint constraint) {
    this(null, project, name, motivation, semanticInterventionLabel, semanticInterventionUri, constraint);
  }
}
