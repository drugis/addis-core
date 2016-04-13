package org.drugis.addis.interventions.service.impl;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.FixedDoseIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.model.TitratedDoseIntervention;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.trialverse.model.TrialDataArm;
import org.springframework.stereotype.Service;

/**
 * Created by connor on 12-4-16.
 */
@Service
public class InterventionServiceImpl implements InterventionService {
  @Override
  public boolean isMatched(AbstractIntervention intervention, TrialDataArm arm) {
    if(intervention instanceof SimpleIntervention) {
      return intervention.getSemanticInterventionUri().equals(arm.getSemanticIntervention().getDrugConcept());
    }
    if(intervention instanceof FixedDoseIntervention) {
      return intervention.getSemanticInterventionUri().equals(arm.getSemanticIntervention().getDrugConcept());
    }
    if(intervention instanceof TitratedDoseIntervention) {
      return intervention.getSemanticInterventionUri().equals(arm.getSemanticIntervention().getDrugConcept());
    }
    return false;
  }
}
