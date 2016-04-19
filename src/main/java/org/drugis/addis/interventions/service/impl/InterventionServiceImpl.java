package org.drugis.addis.interventions.service.impl;

import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.Dose;
import org.drugis.addis.trialverse.model.trialdata.FixedSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.TitratedSemanticIntervention;
import org.springframework.stereotype.Service;

/**
 * Created by connor on 12-4-16.
 */
@Service
public class InterventionServiceImpl implements InterventionService {
  @Override
  public boolean isMatched(AbstractIntervention intervention, AbstractSemanticIntervention semanticIntervention) throws InvalidTypeForDoseCheckException {

    if(intervention instanceof SimpleIntervention) {
      return checkSimple(intervention, semanticIntervention);
    }

    if(intervention instanceof FixedDoseIntervention) {

      return !(!checkType(intervention, semanticIntervention) || !checkSimple(intervention, semanticIntervention) || !doseCheck(intervention, semanticIntervention));
    }

    if(intervention instanceof TitratedDoseIntervention || intervention instanceof BothDoseTypesIntervention) {
      return !(!checkType(intervention, semanticIntervention) || !checkSimple(intervention, semanticIntervention) || !doseCheck(intervention, semanticIntervention));
    }
    return false;
  }

  private boolean checkSimple(AbstractIntervention intervention, AbstractSemanticIntervention semanticIntervention) {
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
        return (minConstraint == null || isValid(minConstraint, dose) && maxConstraint == null || isValid(maxConstraint, dose) );
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


}
