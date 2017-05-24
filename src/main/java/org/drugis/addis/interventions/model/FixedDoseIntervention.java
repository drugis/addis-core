package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.command.InterventionMultiplierCommand;
import org.drugis.addis.interventions.controller.command.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.FixedInterventionViewAdapter;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
@Entity
@PrimaryKeyJoinColumn(name = "fixedInterventionId", referencedColumnName = "singleInterventionId")
public class FixedDoseIntervention extends SingleIntervention implements Serializable {
  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "lowerBound.type", column = @Column(name = "lowerBoundType")),
          @AttributeOverride(name = "lowerBound.unitName", column = @Column(name = "lowerBoundUnitName")),
          @AttributeOverride(name = "lowerBound.unitPeriod", column = @Column(name = "lowerBoundUnitPeriod")),
          @AttributeOverride(name = "lowerBound.unitConcept", column = @Column(name = "lowerBoundUnitConcept")),
          @AttributeOverride(name = "lowerBound.value", column = @Column(name = "lowerBoundValue")),
          @AttributeOverride(name = "lowerBound.conversionMultiplier", column = @Column(name = "lowerBoundConversionMultiplier")),
          @AttributeOverride(name = "upperBound.type", column = @Column(name = "upperBoundType")),
          @AttributeOverride(name = "upperBound.unitName", column = @Column(name = "upperBoundUnitName")),
          @AttributeOverride(name = "upperBound.unitPeriod", column = @Column(name = "upperBoundUnitPeriod")),
          @AttributeOverride(name = "upperBound.unitConcept", column = @Column(name = "upperBoundUnitConcept")),
          @AttributeOverride(name = "upperBound.value", column = @Column(name = "upperBoundValue")),
          @AttributeOverride(name = "upperBound.conversionMultiplier", column = @Column(name = "upperBoundConversionMultiplier"))

  })
  DoseConstraint constraint;

  public FixedDoseIntervention() {
  }

  public DoseConstraint getConstraint() {
    return constraint;
  }

  public FixedDoseIntervention(Integer id, Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel
          , DoseConstraint constraint) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
    this.constraint = constraint;
  }

  public FixedDoseIntervention(Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel,
                               DoseConstraint constraint) {
    this(null, project, name, motivation, semanticInterventionUri, semanticInterventionLabel, constraint);
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new FixedInterventionViewAdapter(this);
  }

  @Override
  public void updateMultipliers(SetMultipliersCommand command) {
    for (InterventionMultiplierCommand multiplierCommand : command.getMultipliers()) {
      Double multiplier = multiplierCommand.getConversionMultiplier();
      URI unitConcept = multiplierCommand.getUnitConcept();
      String unitName = multiplierCommand.getUnitName();
      AbstractIntervention.updateConstraint(constraint, multiplier, unitConcept, unitName);
    }
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
