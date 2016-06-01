package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.controller.command.LowerBoundCommand;
import org.drugis.addis.interventions.controller.command.UpperBoundCommand;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.service.impl.InterventionServiceImpl;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.junit.Test;

import java.net.URI;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 12-4-16.
 */
public class InterventionServiceTest {

  private final URI drugInstanceUri = URI.create("drugInstanceUri");
  private InterventionService interventionService = new InterventionServiceImpl();

  private Integer interventionId = 2;
  private Integer projectId = 1;

  private URI unitConcept = URI.create("unitCons");
  private String unitLabel = "unitLabel";
  private Double unitMultiplier = 2d;
  private URI drugConceptUri = URI.create("drugConceptUri");

  @Test
  public void isMatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label");
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(drugInstanceUri, drugConceptUri);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isUNMatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(drugInstanceUri, drugConceptUri);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);
    URI otherdrugConceptUri = URI.create("otherDrugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", otherdrugConceptUri, "sem label");

    assertFalse(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isMatchedFixedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isNotMatchedFixedIntervention() throws Exception, InvalidTypeForDoseCheckException, InvalidConstraintException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isMatchedTitratedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isUnMatchedTitratedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isMatchedBothIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test
  public void isUnMatchedBothIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm.getSemanticIntervention()));
  }

  @Test

  public void testSpuriousFixedBothMatch() throws InvalidTypeForDoseCheckException, InvalidConstraintException {
    URI gramConcept = URI.create("http://gram");
    Dose dose = new Dose(80d, "P1D", gramConcept, "mg", 1000d);
    AbstractSemanticIntervention fixedSemantic = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, dose);
    LowerBoundCommand overTwenty = new LowerBoundCommand(LowerBoundType.MORE_THAN, 0d, "mg", "P1D", gramConcept);
    UpperBoundCommand atMostEighty = new UpperBoundCommand(UpperBoundType.AT_MOST, 20d, "mg", "P1D", gramConcept);
    DoseConstraint minConstraint = null;
    DoseConstraint maxConstraint = new DoseConstraint(overTwenty, atMostEighty);
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "name", "motive", drugConceptUri, "intervention sem",
            minConstraint,
            maxConstraint);
    assertFalse(interventionService.isMatched(intervention, fixedSemantic));

  }

}