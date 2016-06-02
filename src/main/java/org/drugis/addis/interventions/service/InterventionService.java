package org.drugis.addis.interventions.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;

import java.util.List;

/**
 * Created by connor on 12-4-16.
 */
public interface InterventionService {
  boolean isMatched(AbstractIntervention intervention, List<AbstractSemanticIntervention> semanticIntervention) throws InvalidTypeForDoseCheckException, ResourceDoesNotExistException;
}
