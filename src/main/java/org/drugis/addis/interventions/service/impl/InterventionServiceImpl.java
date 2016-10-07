package org.drugis.addis.interventions.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.Dose;
import org.drugis.addis.trialverse.model.trialdata.FixedSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.TitratedSemanticIntervention;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by connor on 12-4-16.
 */
@Service
public class InterventionServiceImpl implements InterventionService {

  @Inject
  InterventionRepository interventionRepository;

  @Override
  public AbstractIntervention updateNameAndMotivation(Integer projectId, Integer interventionId, String name, String motivation) throws Exception {
    if(interventionRepository.isExistingInterventionName(interventionId, name)){
      throw new Exception("Can not update intervention, intervention name must be unique");
    }
    AbstractIntervention abstractIntervention = interventionRepository.get(projectId, interventionId);
    abstractIntervention.setName(name);
    abstractIntervention.setMotivation(motivation);
    return abstractIntervention;
  }

  public List<SingleIntervention> resolveCombinations(List<CombinationIntervention> combinationInterventions) throws ResourceDoesNotExistException {
    List<SingleIntervention> singleInterventions = new ArrayList<>();
    for(CombinationIntervention combinationIntervention: combinationInterventions){
      singleInterventions.addAll(resolveCombinations(combinationIntervention));
    }
    return singleInterventions;
  }

  @Override
  public Set<SingleIntervention> resolveInterventionSets(List<InterventionSet> interventionSets) throws ResourceDoesNotExistException {
    Set<SingleIntervention> singleInterventions = new HashSet<>();
    for(InterventionSet interventionSet: interventionSets){
      singleInterventions.addAll(resolveInterventionSet(interventionSet));
    }
    return singleInterventions;
  }

  private Set<? extends SingleIntervention> resolveInterventionSet(InterventionSet interventionSet) throws ResourceDoesNotExistException {
    Set<SingleIntervention> singleInterventions = new HashSet<>();
    for(Integer interventionId: interventionSet.getInterventionIds()){
      AbstractIntervention abstractIntervention = interventionRepository.get(interventionId);
      if(abstractIntervention instanceof SingleIntervention) {
        singleInterventions.add((SingleIntervention) abstractIntervention);
      } else if(abstractIntervention instanceof CombinationIntervention) {
        singleInterventions.addAll(resolveCombinations((CombinationIntervention) abstractIntervention));
      }
    }
    return singleInterventions;
  }

  @Override
  public boolean isMatched(final AbstractIntervention intervention, final List<AbstractSemanticIntervention> semanticInterventions) throws InvalidTypeForDoseCheckException, ResourceDoesNotExistException {

    if(intervention instanceof SingleIntervention) {
      if (semanticInterventions.size() > 1) {
        return false;
      }
      AbstractSemanticIntervention semanticIntervention = semanticInterventions.get(0);
      SingleIntervention singleIntervention = (SingleIntervention) intervention;

      return isSingleInterventionMatched(singleIntervention, semanticIntervention);
    }

    if (intervention instanceof CombinationIntervention) {
      CombinationIntervention combinationIntervention = (CombinationIntervention) intervention;
      if (semanticInterventions.size() != combinationIntervention.getSingleInterventionIds().size()) {
        return false;
      }
      Boolean allMatched = true;
      List<SingleIntervention> singleInterventions = new ArrayList<>();
      for (Integer interventionId : combinationIntervention.getSingleInterventionIds()) {
        singleInterventions.add((SingleIntervention) interventionRepository.get(interventionId));
      }
      // find matching semantic intervention for each addis intervention
      // if found remove from sem. interventions
      // if not found return false
      // return true if list empty
      ArrayList<AbstractSemanticIntervention> semanticInterventionsToMatch = new ArrayList<>(semanticInterventions);
      for (SingleIntervention singleIntervention : singleInterventions) {
        Boolean found = false;
        for (AbstractSemanticIntervention semanticIntervention : semanticInterventionsToMatch) {
          if (isSingleInterventionMatched(singleIntervention, semanticIntervention)) {
            found = true;
            semanticInterventionsToMatch.remove(semanticIntervention);
            break;
          }
        }
        if (!found) {
          return false;
        }
      }
      return semanticInterventionsToMatch.size() == 0; // all semantic interventions matched
    }

    if(intervention instanceof InterventionSet) {
      InterventionSet interventionSet = (InterventionSet) intervention;

      List<AbstractIntervention> interventions = new ArrayList<>();
      for(Integer interventionId: interventionSet.getInterventionIds()) {
        interventions.add(interventionRepository.get(interventionId));
      }

      for(AbstractIntervention interventionToMatch: interventions) {
        if(isMatched(interventionToMatch, semanticInterventions)) {
          return true;
        }
      }
      return false;
    }
    return false;
  }

  private Boolean isSingleInterventionMatched(SingleIntervention singleIntervention, AbstractSemanticIntervention semanticIntervention) throws InvalidTypeForDoseCheckException {
    if (singleIntervention instanceof SimpleIntervention) {
      return checkSimple(singleIntervention, semanticIntervention);
    }

    if (singleIntervention instanceof FixedDoseIntervention) {

      return !(!checkType(singleIntervention, semanticIntervention) || !checkSimple(singleIntervention, semanticIntervention) || !doseCheck(singleIntervention, semanticIntervention));
    }

    if (singleIntervention instanceof TitratedDoseIntervention || singleIntervention instanceof BothDoseTypesIntervention) {
      return !(!checkType(singleIntervention, semanticIntervention) || !checkSimple(singleIntervention, semanticIntervention) || !doseCheck(singleIntervention, semanticIntervention));
    }
    return false;
  }

  private boolean checkSimple(SingleIntervention intervention, AbstractSemanticIntervention semanticIntervention) {
    return intervention.getSemanticInterventionUri().equals(semanticIntervention.getDrugConcept());
  }

  private boolean checkType(AbstractIntervention intervention, AbstractSemanticIntervention semanticIntervention) {
    return (intervention instanceof FixedDoseIntervention && semanticIntervention instanceof FixedSemanticIntervention ||
            intervention instanceof TitratedDoseIntervention && semanticIntervention instanceof TitratedSemanticIntervention ||
            intervention instanceof BothDoseTypesIntervention && semanticIntervention instanceof FixedSemanticIntervention ||
            intervention instanceof BothDoseTypesIntervention && semanticIntervention instanceof TitratedSemanticIntervention);
  }

  private boolean doseCheck(AbstractIntervention intervention, AbstractSemanticIntervention semanticIntervention) throws InvalidTypeForDoseCheckException {

    if(intervention instanceof FixedDoseIntervention) {
      DoseConstraint constraint = ((FixedDoseIntervention) intervention).getConstraint();
      Dose dose = ((FixedSemanticIntervention) semanticIntervention).getDose();
      return isValid(constraint, dose);
    }

    if(intervention instanceof TitratedDoseIntervention) {

      DoseConstraint minConstraint = ((TitratedDoseIntervention) intervention).getMinConstraint();
      Dose minDose = ((TitratedSemanticIntervention) semanticIntervention).getMinDose();
      boolean isValidMinConstraint = minConstraint == null || isValid(minConstraint, minDose);

      DoseConstraint maxConstraint = ((TitratedDoseIntervention) intervention).getMaxConstraint();
      Dose maxDose = ((TitratedSemanticIntervention) semanticIntervention).getMaxDose();
      boolean isValidMaxConstraint = maxConstraint == null || isValid(maxConstraint, maxDose);

      return isValidMinConstraint && isValidMaxConstraint;

    }

    if(intervention instanceof BothDoseTypesIntervention) {

      DoseConstraint minConstraint = ((BothDoseTypesIntervention) intervention).getMinConstraint();
      DoseConstraint maxConstraint = ((BothDoseTypesIntervention) intervention).getMaxConstraint();

      if(semanticIntervention instanceof FixedSemanticIntervention){
        Dose dose = ((FixedSemanticIntervention) semanticIntervention).getDose();
        boolean isValidMinConstraint = minConstraint == null || isValid(minConstraint, dose);
        boolean isValidMaxConstraint = maxConstraint == null || isValid(maxConstraint, dose);
        return isValidMinConstraint && isValidMaxConstraint;
      }

      if(semanticIntervention instanceof TitratedSemanticIntervention){

        Dose minDose = ((TitratedSemanticIntervention) semanticIntervention).getMinDose();
        boolean isValidMinConstraint = minConstraint == null || isValid(minConstraint, minDose);

        Dose maxDose = ((TitratedSemanticIntervention) semanticIntervention).getMaxDose();
        boolean isValidMaxConstraint = maxConstraint == null || isValid(maxConstraint, maxDose);

        return isValidMinConstraint && isValidMaxConstraint;
      }
    }


    throw new InvalidTypeForDoseCheckException();

  }

  private boolean isValid(DoseConstraint constraint, Dose dose) {
    LowerDoseBound lowerBound = constraint.getLowerBound();
    UpperDoseBound upperBound = constraint.getUpperBound();

    // check unit
    if(lowerBound != null && !dose.getUnitConceptUri().equals(lowerBound.getUnitConcept())){
      return false;
    }

    if(upperBound != null && !dose.getUnitConceptUri().equals(upperBound.getUnitConcept())){
      return false;
    }

    //check period
    if(lowerBound != null && !dose.getPeriodicity().equals(lowerBound.getUnitPeriod())){
      return false;
    }
    if(upperBound != null && !dose.getPeriodicity().equals(upperBound.getUnitPeriod())){
      return false;
    }

    //value
    if(lowerBound != null && !isWithinConstraint(dose.getValue(), constraint)) {
      return false;
    }
    if(upperBound != null && !isWithinConstraint(dose.getValue(), constraint)) {
      return false;
    }

    return true;
  }

  private boolean isWithinConstraint(Double value, DoseConstraint constraint) {
    LowerDoseBound lowerBound = constraint.getLowerBound();
    UpperDoseBound upperBound = constraint.getUpperBound();
    boolean validUpperBound = true;
    boolean validLowerBound = true;

    if(lowerBound != null) {
      validLowerBound = lowerBound.getType().isValidForBound(value, lowerBound.getValue());
    }

    if(upperBound != null) {
      validUpperBound = upperBound.getType().isValidForBound(value, upperBound.getValue());
    }

    return validLowerBound && validUpperBound;
  }

  private List<SingleIntervention> resolveCombinations(CombinationIntervention combinationIntervention) throws ResourceDoesNotExistException {
    List<SingleIntervention> singleInterventions = new ArrayList<>();
    for(Integer singleInterventionId: combinationIntervention.getSingleInterventionIds()){
      singleInterventions.add((SingleIntervention) interventionRepository.get(singleInterventionId));
    }
    return singleInterventions;
  }

}
