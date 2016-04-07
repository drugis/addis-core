package org.drugis.addis.interventions.model;

import org.drugis.addis.trialverse.model.SemanticIntervention;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import java.io.Serializable;

/**
 * Created by daan on 3/6/14.
 */
@Entity
@PrimaryKeyJoinColumn(name = "simpleInterventionId", referencedColumnName = "id")
public class SimpleIntervention extends AbstractIntervention implements Serializable {

  public SimpleIntervention() {
  }

  public SimpleIntervention(Integer id, Integer project, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUri) {
    super(id, project, name, motivation, semanticInterventionLabel, semanticInterventionUri);
  }

  public SimpleIntervention(Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(null, projectId, name, motivation, semanticIntervention.getUri(), semanticIntervention.getLabel());
  }

  public SimpleIntervention(Integer id, Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(id, projectId, name, motivation, semanticIntervention.getUri(), semanticIntervention.getLabel());
  }
}
