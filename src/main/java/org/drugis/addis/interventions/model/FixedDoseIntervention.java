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
          @AttributeOverride(name="lowerBound.unitName" , column = @Column(name="lowerBoundUnitName") ),
          @AttributeOverride(name="lowerBound.unitPeriod" , column = @Column(name="lowerBoundUnitPeriod") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="lowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="upperBoundType") ),
          @AttributeOverride(name="upperBound.unitName" , column = @Column(name="upperBoundUnitName") ),
          @AttributeOverride(name="upperBound.unitPeriod" , column = @Column(name="upperBoundUnitPeriod") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="upperBoundValue") )
  } )
  DoseConstraint constraint;

  public FixedDoseIntervention() {
  }

  public DoseConstraint getConstraint() {
    return constraint;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    FixedDoseIntervention that = (FixedDoseIntervention) o;

    return constraint.equals(that.constraint);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + constraint.hashCode();
    return result;
  }
}
