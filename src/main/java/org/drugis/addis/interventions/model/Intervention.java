package org.drugis.addis.interventions.model;

import org.drugis.addis.trialverse.model.SemanticIntervention;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by daan on 3/6/14.
 */
@Entity
public class Intervention extends AbstractIntervention implements Serializable {

  public Intervention() {
  }

  public Intervention(Integer id, Integer project, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUri) {
    super(id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri);
  }

  public Intervention(Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(null, projectId, name, motivation, semanticIntervention.getUri(), semanticIntervention.getLabel());
  }

  public Intervention(Integer id, Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(id, projectId, name, motivation, semanticIntervention.getUri(), semanticIntervention.getLabel());
  }
}
