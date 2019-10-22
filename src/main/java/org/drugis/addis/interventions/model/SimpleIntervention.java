package org.drugis.addis.interventions.model;

import org.apache.commons.lang3.NotImplementedException;
import org.drugis.addis.interventions.controller.command.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.SimpleInterventionViewAdapter;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by daan on 3/6/14.
 */
@Entity
@PrimaryKeyJoinColumn(name = "simpleInterventionId", referencedColumnName = "singleInterventionId")
public class SimpleIntervention extends SingleIntervention implements Serializable {

  public SimpleIntervention() {
  }

  public SimpleIntervention(Integer id, Integer project, String name, String motivation, URI semanticInterventionUri, String semanticInterventionLabel) {
    super(id, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new SimpleInterventionViewAdapter(this);
  }

  @Override
  public void updateMultipliers(SetMultipliersCommand command) {
    throw new NotImplementedException("not implemented"); // does not have multiplier
  }
  public SimpleIntervention(Integer projectId, String name, String motivation, SemanticInterventionUriAndName semanticInterventionUriAndName) {
    this(null, projectId, name, motivation, semanticInterventionUriAndName.getUri(), semanticInterventionUriAndName.getLabel());
  }

  public SimpleIntervention(Integer id, Integer projectId, String name, String motivation, SemanticInterventionUriAndName semanticInterventionUriAndName) {
    this(id, projectId, name, motivation, semanticInterventionUriAndName.getUri(), semanticInterventionUriAndName.getLabel());
  }


}
