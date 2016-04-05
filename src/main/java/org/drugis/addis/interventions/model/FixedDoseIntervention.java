package org.drugis.addis.interventions.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class FixedDoseIntervention extends AbstractIntervention implements Serializable {
  @Embedded
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
