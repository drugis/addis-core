package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.service.impl.InterventionServiceImpl;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.*;
import org.junit.Test;

import java.net.URI;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 12-4-16.
 */
public class InterventionServiceTest {

  private InterventionService interventionService = new InterventionServiceImpl();

  private Integer interventionId = 2;
  private Integer projectId = 1;

  private Integer sampleSize = 200;
  private Integer rate = 30;
  private Double std = 0.5;
  private Double mean = 30.2;

  private URI unitConcept = URI.create("unitCons");
  private String unitLabel = "unitLabel";
  private Double unitMultiplier = 2d;
  Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
  private URI drugConceptUri = URI.create("drugConceptUri");

  @Test
  public void isMatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label");
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isUNMatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri);
    Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);
    URI otherdrugConceptUri = URI.create("otherDrugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", otherdrugConceptUri, "sem label");

    assertFalse(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isMatchedFixedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, fixedMatchingDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isNotMatchedFixedIntervention() throws Exception, InvalidTypeForDoseCheckException, InvalidConstraintException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, fixedMatchingDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isMatchedTitratedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isUnMatchedTitratedIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint =  new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint =  new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isMatchedBothIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isUnMatchedBothIntervention() throws Exception, InvalidConstraintException, InvalidTypeForDoseCheckException {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept);
    DoseConstraint minConstraint =  new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint =  new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, minDose, maxDose);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertFalse(interventionService.isMatched(intervention, trialdataArm));
  }


}