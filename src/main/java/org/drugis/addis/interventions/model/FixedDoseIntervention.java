package org.drugis.addis.interventions.model;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by daan on 5-4-16.
 */
@Entity
public class FixedDoseIntervention extends AbstractIntervention implements Serializable {
  Double testValue;

  public FixedDoseIntervention() {
  }

  public FixedDoseIntervention(Double testValue) {
    this.testValue = testValue;
  }

  public FixedDoseIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUri, Double testValue) {
    super(id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri);
    this.testValue = testValue;
  }
}
