package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.service.impl.InterventionServiceImpl;
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

  private URI unitConcepts = URI.create("unitCons");
  private String unitLabel = "unitLabel";
  private Double unitMultiplier = 2d;

  private URI drugConceptUri = URI.create("drugConceptUri");

  @Test
  public void isMatchedSimpleIntervention() throws Exception {
    URI drugConceptUri = URI.create("drugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label");
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri);
    Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isUNMatchedSimpleIntervention() throws Exception {
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri);
    Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    URI otherdrugConceptUri = URI.create("otherDrugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", otherdrugConceptUri, "sem label");

    assertFalse(interventionService.isMatched(intervention, trialdataArm));
  }

  @Test
  public void isMatchedFixedIntervention() throws Exception {
    URI drugConceptUri = URI.create("drugConceptUri");
    Double doseValue = 123d;
    String dosePeriod = "P1D";
    URI gramConcept = URI.create("gramConcept");

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, gramConcept);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, gramConcept);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);

    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcepts, unitLabel, unitMultiplier);

    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri, fixedMatchingDose);
    Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    assertTrue(interventionService.isMatched(intervention, trialdataArm));
  }

}