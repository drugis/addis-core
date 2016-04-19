package org.drugis.addis.interventions.model;

import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by daan on 3/6/14.
 */
@Entity
@PrimaryKeyJoinColumn(name = "simpleInterventionId", referencedColumnName = "id")
public class SimpleIntervention extends AbstractIntervention implements Serializable {

  public SimpleIntervention() {
  }

  public SimpleIntervention(Integer id, Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
  }

  public SimpleIntervention(Integer projectId, String name, String motivation, SemanticInterventionUriAndName semanticInterventionUriAndName) {
    this(null, projectId, name, motivation, semanticInterventionUriAndName.getUri(), semanticInterventionUriAndName.getLabel());
  }

  public SimpleIntervention(Integer id, Integer projectId, String name, String motivation, SemanticInterventionUriAndName semanticInterventionUriAndName) {
    this(id, projectId, name, motivation, semanticInterventionUriAndName.getUri(), semanticInterventionUriAndName.getLabel());
  }
}
