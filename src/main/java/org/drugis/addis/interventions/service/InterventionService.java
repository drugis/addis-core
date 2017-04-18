package org.drugis.addis.interventions.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;

import java.net.URI;
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

  void delete(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException;

  static AbstractInterventionCommand buildSingleInterventionCommand(Integer newProjectId, SingleIntervention intervention) throws InvalidConstraintException {
    if (intervention instanceof SimpleIntervention) {
      return new SimpleInterventionCommand(newProjectId, intervention.getName(), intervention.getMotivation(),
              intervention.getSemanticInterventionUri().toString(), intervention.getSemanticInterventionLabel());
    } else if (intervention instanceof FixedDoseIntervention) {
      FixedDoseIntervention cast = (FixedDoseIntervention) intervention;
      ConstraintCommand constraintCommand = buildConstraintCommand(cast.getConstraint());
      return new FixedInterventionCommand(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionLabel(),
              cast.getSemanticInterventionUri().toString(), constraintCommand);
    } else if (intervention instanceof TitratedDoseIntervention) {
      TitratedDoseIntervention cast = (TitratedDoseIntervention) intervention;
      ConstraintCommand minConstraintCommand = buildConstraintCommand(cast.getMinConstraint());
      ConstraintCommand maxConstraintCommand = buildConstraintCommand(cast.getMaxConstraint());
      return new TitratedInterventionCommand(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionLabel(),
              cast.getSemanticInterventionUri().toString(), minConstraintCommand, maxConstraintCommand);
    } else if (intervention instanceof BothDoseTypesIntervention) {
      BothDoseTypesIntervention cast = (BothDoseTypesIntervention) intervention;
      ConstraintCommand minConstraintCommand = buildConstraintCommand(cast.getMinConstraint());
      ConstraintCommand maxConstraintCommand = buildConstraintCommand(cast.getMaxConstraint());
      return new BothDoseTypesInterventionCommand(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(),
              cast.getSemanticInterventionLabel(), minConstraintCommand, maxConstraintCommand);
    }
    return null;
  }
  static ConstraintCommand buildConstraintCommand(DoseConstraint constraint) {
    if (constraint == null) return null;

    LowerDoseBound oldLower = constraint.getLowerBound();
    LowerBoundCommand lowerBoundCommand = oldLower == null ? null : new LowerBoundCommand(oldLower.getType(), oldLower.getValue(), oldLower.getUnitName(),
            oldLower.getUnitPeriod(), oldLower.getUnitConcept());
    UpperDoseBound oldUpper = constraint.getUpperBound();
    UpperBoundCommand upperBoundCommand = oldUpper == null ? null : new UpperBoundCommand(oldUpper.getType(), oldUpper.getValue(), oldUpper.getUnitName(),
            oldUpper.getUnitPeriod(), oldUpper.getUnitConcept());
    return new ConstraintCommand(lowerBoundCommand, upperBoundCommand);
  }

  void setMultipliers(Integer interventionId, SetMultipliersCommand command) throws ResourceDoesNotExistException;
}
