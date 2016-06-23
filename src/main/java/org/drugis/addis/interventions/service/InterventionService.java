package org.drugis.addis.interventions.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;

import java.util.List;

/**
 * Created by connor on 12-4-16.
 */
public interface InterventionService {
  List<SingleIntervention> resolveCombinations(List<CombinationIntervention> combinationInterventions) throws ResourceDoesNotExistException;
  boolean isMatched(AbstractIntervention intervention, List<AbstractSemanticIntervention> semanticIntervention) throws InvalidTypeForDoseCheckException, ResourceDoesNotExistException;
  AbstractIntervention updateNameAndMotivation(Integer projectId, Integer interventionId, String name, String motivation) throws Exception;
}
