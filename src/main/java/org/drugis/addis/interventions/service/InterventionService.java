package org.drugis.addis.interventions.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.InterventionSet;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 12-4-16.
 */
public interface InterventionService {
  List<SingleIntervention> resolveCombinations(List<CombinationIntervention> combinationInterventions) throws ResourceDoesNotExistException;
  Set<SingleIntervention> resolveInterventionSets(List<InterventionSet> interventionSets) throws ResourceDoesNotExistException;
  boolean isMatched(AbstractIntervention intervention, List<AbstractSemanticIntervention> semanticIntervention) throws InvalidTypeForDoseCheckException, ResourceDoesNotExistException;
  AbstractIntervention updateNameAndMotivation(Integer projectId, Integer interventionId, String name, String motivation) throws Exception;
}
