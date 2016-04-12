package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.trialverse.model.TrialDataArm;

/**
 * Created by connor on 12-4-16.
 */
public interface InterventionService {
  boolean isMatched(AbstractIntervention intervention, TrialDataArm arm);
}
