package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.TitratedInterventionViewAdapter;

import javax.persistence.*;
import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
@Entity
@PrimaryKeyJoinColumn(name = "titratedInterventionId", referencedColumnName = "singleInterventionId")
public class TitratedDoseIntervention extends SingleIntervention {
  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="minLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unitName" , column = @Column(name="minLowerBoundUnitName") ),
          @AttributeOverride(name="lowerBound.unitPeriod" , column = @Column(name="minLowerBoundUnitPeriod") ),
          @AttributeOverride(name="lowerBound.unitConcept" , column = @Column(name="minLowerBoundUnitConcept") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="minLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="minUpperBoundType") ),
          @AttributeOverride(name="upperBound.unitName" , column = @Column(name="minUpperBoundUnitName") ),
          @AttributeOverride(name="upperBound.unitPeriod" , column = @Column(name="minUpperBoundUnitPeriod") ),
          @AttributeOverride(name="upperBound.unitConcept" , column = @Column(name="minUpperBoundUnitConcept") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="minUpperBoundValue") )
  } )
  private DoseConstraint minConstraint;

  @Embedded
  @AttributeOverrides( {
          @AttributeOverride(name="lowerBound.type" , column = @Column(name="maxLowerBoundType") ),
          @AttributeOverride(name="lowerBound.unitName" , column = @Column(name="maxLowerBoundUnitName") ),
          @AttributeOverride(name="lowerBound.unitPeriod" , column = @Column(name="maxLowerBoundUnitPeriod") ),
          @AttributeOverride(name="lowerBound.unitConcept" , column = @Column(name="maxLowerBoundUnitConcept") ),
          @AttributeOverride(name="lowerBound.value", column = @Column(name="maxLowerBoundValue") ),
          @AttributeOverride(name="upperBound.type" , column = @Column(name="maxUpperBoundType") ),
          @AttributeOverride(name="upperBound.unitName" , column = @Column(name="maxUpperBoundUnitName") ),
          @AttributeOverride(name="upperBound.unitPeriod" , column = @Column(name="maxUpperBoundUnitPeriod") ),
          @AttributeOverride(name="upperBound.unitConcept" , column = @Column(name="maxUpperBoundUnitConcept") ),
          @AttributeOverride(name="upperBound.value", column = @Column(name="maxUpperBoundValue") )
  } )
  private DoseConstraint maxConstraint;

  public TitratedDoseIntervention() {
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new TitratedInterventionViewAdapter(this);
  }

  public TitratedDoseIntervention(Integer id, Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) throws InvalidConstraintException {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);

    if(minConstraint == null && maxConstraint == null) {
      throw new InvalidConstraintException("invalid TitratedDoseIntervention both min- and maxConstraint are null");
    }

    this.minConstraint = minConstraint;
    this.maxConstraint = maxConstraint;
  }

  public TitratedDoseIntervention(Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel, DoseConstraint minConstraint, DoseConstraint maxConstraint) throws InvalidConstraintException {
    this(null, project, name, motivation, semanticInterventionUri, semanticInterventionLabel, minConstraint, maxConstraint);
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
