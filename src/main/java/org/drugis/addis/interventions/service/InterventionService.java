package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.AbstractSemanticIntervention;

/**
 * Created by connor on 12-4-16.
 */
public interface InterventionService {
  boolean isMatched(AbstractIntervention intervention, AbstractSemanticIntervention semanticIntervention) throws InvalidTypeForDoseCheckException;
}
