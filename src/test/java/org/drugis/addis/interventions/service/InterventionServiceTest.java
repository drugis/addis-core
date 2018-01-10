package org.drugis.addis.interventions.service;

import com.google.common.collect.ImmutableSet;
import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.model.InterventionInclusion;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.InterventionMultiplierCommand;
import org.drugis.addis.interventions.controller.command.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.command.LowerBoundCommand;
import org.drugis.addis.interventions.controller.command.UpperBoundCommand;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.impl.InterventionServiceImpl;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.social.OperationNotPermittedException;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 12-4-16.
 */
@SuppressWarnings("ConstantConditions")
public class InterventionServiceTest {

  private final URI drugInstanceUri = URI.create("drugInstanceUri");

  @Mock
  private InterventionRepository interventionRepository;

  @Mock
  private AnalysisRepository analysisRepository;

  @InjectMocks
  private InterventionService interventionService;

  private Integer interventionId = 2;
  private Integer projectId = 1;

  private URI unitConcept = URI.create("unitCons");
  private String unitLabel = "unitLabel";
  private Double unitMultiplier = 2d;
  private URI drugConceptUri = URI.create("drugConceptUri");
  private URI drugConceptUri2 = URI.create("drugConceptUri2");
  private final Double doseValue = 123d;
  private final String dosePeriod = "P1D";

  @Before
  public void setUp() {
    interventionService = new InterventionServiceImpl();
    initMocks(this);
  }

  @Test
  public void isMatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label");
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(drugInstanceUri, drugConceptUri);

    assertTrue(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isUnmatchedSimpleIntervention() throws Exception, InvalidTypeForDoseCheckException {
    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(drugInstanceUri, drugConceptUri);
    URI otherdrugConceptUri = URI.create("otherDrugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", otherdrugConceptUri, "sem label");

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isMatchedFixedIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel,
            dosePeriod, unitConcept, 0.1);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel,
            dosePeriod, unitConcept, 1000.0);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name",
            "moti", drugConceptUri, "sem label", doseConstraint);

    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri,
            fixedMatchingDose);

    assertTrue(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isMatchedFixedInterventionWeeks() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel,
            "P1W", unitConcept, 0.1);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel,
            "P1W", unitConcept, 1000.0);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name",
            "moti", drugConceptUri, "sem label", doseConstraint);

    Dose fixedMatchingDose = new Dose(doseValue, "P7D", unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri,
            fixedMatchingDose);

    assertTrue(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }



  @Test
  public void precisionTest() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = null;
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.LESS_THAN, 1.8, unitLabel,
            dosePeriod, unitConcept, 0.001);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name",
            "moti", drugConceptUri, "sem label", doseConstraint);

    Dose fixedMatchingDose = new Dose(0.0018, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri,
            fixedMatchingDose);

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isNotMatchedFixedIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue + 1, unitLabel,
            dosePeriod, unitConcept, 0.5);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel,
            dosePeriod, unitConcept, null);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isMatchedTitratedIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel,
            dosePeriod, unitConcept, 0.2);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel,
            dosePeriod, unitConcept, 5.0);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);

    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);

    assertTrue(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isUnmatchedTitratedIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, null);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept, null);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isMatchedBothIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);

    assertTrue(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void isUnMatchedBothIntervention() throws Exception, InvalidTypeForDoseCheckException {
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, null);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue - 999, unitLabel, dosePeriod, unitConcept, null);
    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", minConstraint, maxConstraint);
    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention semanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(semanticIntervention)));
  }

  @Test
  public void testSpuriousFixedBothMatch() throws InvalidTypeForDoseCheckException, InvalidConstraintException, ResourceDoesNotExistException {
    URI gramConcept = URI.create("http://gram");
    Dose dose = new Dose(80d, "P1D", gramConcept, "mg", 1000d);
    AbstractSemanticIntervention fixedSemantic = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, dose);
    LowerBoundCommand overTwenty = new LowerBoundCommand(LowerBoundType.MORE_THAN, 0d, "mg", "P1D", gramConcept, null);
    UpperBoundCommand atMostEighty = new UpperBoundCommand(UpperBoundType.AT_MOST, 20d, "mg", "P1D", gramConcept, null);
    DoseConstraint minConstraint = null;
    DoseConstraint maxConstraint = new DoseConstraint(overTwenty, atMostEighty);
    AbstractIntervention intervention = new BothDoseTypesIntervention(interventionId, projectId, "name", "motive", drugConceptUri, "intervention sem",
            minConstraint,
            maxConstraint);

    assertFalse(interventionService.isMatched(intervention, Collections.singletonList(fixedSemantic)));
  }

  @Test
  public void isMatchedCombinationIntervention() throws ResourceDoesNotExistException, InvalidTypeForDoseCheckException, InvalidConstraintException {
    Integer fixedInterventionId = 1;
    Integer titratedInterventionId = 2;

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention fixedIntervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);

    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention titratedIntervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri2, "sem label", minConstraint, maxConstraint);

    when(interventionRepository.get(fixedInterventionId)).thenReturn(fixedIntervention);
    when(interventionRepository.get(titratedInterventionId)).thenReturn(titratedIntervention);

    AbstractIntervention intervention = new CombinationIntervention(null, null, "combi", null, ImmutableSet.of(fixedInterventionId, titratedInterventionId));
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention fixedSemanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);

    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention titratedSemanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri2, minDose, maxDose);

    List<AbstractSemanticIntervention> semanticInterventions = Arrays.asList(fixedSemanticIntervention, titratedSemanticIntervention);

    assertTrue(interventionService.isMatched(intervention, semanticInterventions));
  }

  @Test
  public void isUnmatchedCombinationIntervention() throws ResourceDoesNotExistException, InvalidTypeForDoseCheckException, InvalidConstraintException {
    Integer fixedInterventionId = 1;
    Integer titratedInterventionId = 2;

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention fixedIntervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);

    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention titratedIntervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri2, "sem label", minConstraint, maxConstraint);

    when(interventionRepository.get(fixedInterventionId)).thenReturn(fixedIntervention);
    when(interventionRepository.get(titratedInterventionId)).thenReturn(titratedIntervention);

    AbstractIntervention intervention = new CombinationIntervention(null, null, "combi", null, ImmutableSet.of(fixedInterventionId, titratedInterventionId));
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention fixedSemanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);

    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention titratedSemanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri, minDose, maxDose);

    List<AbstractSemanticIntervention> semanticInterventions = Arrays.asList(fixedSemanticIntervention, titratedSemanticIntervention);

    assertFalse(interventionService.isMatched(intervention, semanticInterventions));
  }

  @Test
  public void isMatchedInterventionSet() throws InvalidConstraintException, ResourceDoesNotExistException, InvalidTypeForDoseCheckException {
    Integer fixedInterventionId = 1;
    Integer titratedInterventionId = 2;
    Integer combinationInterventionId = 3;

    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, doseValue - 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, doseValue + 1, unitLabel, dosePeriod, unitConcept, unitMultiplier);
    DoseConstraint doseConstraint = new DoseConstraint(lowerBound, upperBound);
    AbstractIntervention fixedIntervention = new FixedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label", doseConstraint);

    DoseConstraint minConstraint = new DoseConstraint(lowerBound, upperBound);
    DoseConstraint maxConstraint = null;
    AbstractIntervention titratedIntervention = new TitratedDoseIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri2, "sem label", minConstraint, maxConstraint);

    AbstractIntervention combinationIntervention = new CombinationIntervention(combinationInterventionId, null, "combi", null, ImmutableSet.of(fixedInterventionId, titratedInterventionId));

    when(interventionRepository.get(fixedInterventionId)).thenReturn(fixedIntervention);
    when(interventionRepository.get(titratedInterventionId)).thenReturn(titratedIntervention);
    when(interventionRepository.get(combinationInterventionId)).thenReturn(combinationIntervention);

    AbstractIntervention intervention = new InterventionSet(null, null, "combi", null, ImmutableSet.of(combinationInterventionId, titratedInterventionId));
    Dose fixedMatchingDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention fixedSemanticIntervention = new FixedSemanticIntervention(drugInstanceUri, drugConceptUri, fixedMatchingDose);

    Dose minDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    Dose maxDose = new Dose(doseValue, dosePeriod, unitConcept, unitLabel, unitMultiplier);
    AbstractSemanticIntervention titratedSemanticIntervention = new TitratedSemanticIntervention(drugInstanceUri, drugConceptUri2, minDose, maxDose);

    List<AbstractSemanticIntervention> semanticInterventions = Arrays.asList(fixedSemanticIntervention, titratedSemanticIntervention);

    assertTrue(interventionService.isMatched(intervention, semanticInterventions));
  }

  @Test
  public void editNameAndMotivation() throws Exception {
    String name = "name";
    String motivation = "motivation";
    AbstractIntervention oldIntervention = new SimpleIntervention(interventionId, 123, "oldName", "oldMotivation", URI.create("uri"), "uriLabel");
    when(interventionRepository.get(projectId, interventionId)).thenReturn(oldIntervention);
    when(interventionRepository.isExistingInterventionName(interventionId, "name")).thenReturn(false);
    AbstractIntervention abstractIntervention = interventionService.updateNameAndMotivation(projectId, interventionId, name, motivation);
    assertEquals(interventionId, abstractIntervention.getId());
    assertEquals(name, abstractIntervention.getName());
    assertEquals(motivation, abstractIntervention.getMotivation());
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationCheckDuplicateName() throws Exception {
    String name = "name";
    String motivation = "motivation";
    when(interventionRepository.isExistingInterventionName(interventionId, "name")).thenReturn(true);
    interventionService.updateNameAndMotivation(projectId, interventionId, name, motivation);
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationOtherProject() throws Exception {
    String name = "name";
    String motivation = "motivation";
    when(interventionRepository.isExistingInterventionName(interventionId, "name")).thenReturn(false);
    when(interventionRepository.get(projectId, interventionId)).thenReturn(null);
    interventionService.updateNameAndMotivation(projectId, interventionId, name, motivation);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void deleteUsedIntervention() throws ResourceDoesNotExistException {
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    InterventionInclusion inclusion1 = new InterventionInclusion(analysisId1, interventionId);
    List<InterventionInclusion> interventionInclusions = Collections.singletonList(inclusion1);
    AbstractAnalysis analysisWithInclusion = new NetworkMetaAnalysis(analysisId1, projectId, "analysisWithInclusion",
            Collections.emptyList(), interventionInclusions, Collections.emptyList(), null);
    AbstractAnalysis analysisWithoutInclusions = new NetworkMetaAnalysis(analysisId2, projectId, "analysisWithoutInclusion");
    when(analysisRepository.query(projectId)).thenReturn(Arrays.asList(analysisWithInclusion, analysisWithoutInclusions));

    interventionService.delete(projectId, interventionId);
  }

  @Test
  public void deleteUnusedIntervention() throws ResourceDoesNotExistException {
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    Integer differentInterventionId = -432;
    InterventionInclusion inclusion1 = new InterventionInclusion(analysisId1, differentInterventionId);
    List<InterventionInclusion> interventionInclusions = Collections.singletonList(inclusion1);
    AbstractAnalysis analysisWithInclusion = new NetworkMetaAnalysis(analysisId1, projectId, "analysisWithInclusion",
            Collections.emptyList(), interventionInclusions, Collections.emptyList(), null);
    AbstractAnalysis analysisWithoutInclusions = new NetworkMetaAnalysis(analysisId2, projectId, "analysisWithoutInclusion");
    when(analysisRepository.query(projectId)).thenReturn(Arrays.asList(analysisWithInclusion, analysisWithoutInclusions));

    interventionService.delete(projectId, interventionId);

    verify(analysisRepository).query(projectId);
  }

  @Test
  public void testSetFixedDoseMultipliers() throws ResourceDoesNotExistException, InvalidConstraintException {
    InterventionMultiplierCommand multiplier = new InterventionMultiplierCommand("milligram",
            URI.create("http://concept.com"), 0.001);
    List<InterventionMultiplierCommand> multipliers = Collections.singletonList(multiplier);
    SetMultipliersCommand command = new SetMultipliersCommand(multipliers);
    LowerBoundCommand lowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 1., "milligram", "P1D",
            URI.create("http://concept.com"), null);
    UpperBoundCommand upperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 5., "nanogram", "P1D",
            URI.create("http://concept.com"), null);
    DoseConstraint constraint = new DoseConstraint(lowerBound, upperBound);
    FixedDoseIntervention fixedIntervention = new FixedDoseIntervention(2, 1, "paraffin", "no motivation",
            URI.create("semanticURI"), "semanticInterventionLabel",
            constraint);
    when(interventionRepository.get(interventionId)).thenReturn(
            fixedIntervention);

    interventionService.setMultipliers(interventionId, command);

    assertEquals(multiplier.getConversionMultiplier(), fixedIntervention.getConstraint().getLowerBound().getConversionMultiplier());
    assertNull(fixedIntervention.getConstraint().getUpperBound().getConversionMultiplier());
    verify(interventionRepository).get(interventionId);
  }

  @Test
  public void testSetTitratedDoseMultipliers() throws ResourceDoesNotExistException, InvalidConstraintException {
    InterventionMultiplierCommand multiplier = new InterventionMultiplierCommand("milligram",
            URI.create("http://concept.com"), 0.001);
    List<InterventionMultiplierCommand> multipliers = Collections.singletonList(multiplier);
    SetMultipliersCommand command = new SetMultipliersCommand(multipliers);
    LowerBoundCommand minLowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 1., "milligram", "P1D",
            URI.create("http://concept.com"), null);
    UpperBoundCommand minUpperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 5., "nanogram", "P1D",
            URI.create("http://concept.com"), null);
    LowerBoundCommand maxLowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 1., "nanogram", "P1D",
            URI.create("http://concept.com"), null);
    UpperBoundCommand maxUpperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 5., "milligram", "P1D",
            URI.create("http://concept.com"), null);
    DoseConstraint minConstraint = new DoseConstraint(minLowerBound, minUpperBound);
    DoseConstraint maxConstraint = new DoseConstraint(maxLowerBound, maxUpperBound);
    TitratedDoseIntervention titratedDoseIntervention = new TitratedDoseIntervention(2, 1, "paraffin", "no motivation",
            URI.create("semanticURI"), "semanticInterventionLabel",
            minConstraint, maxConstraint);
    when(interventionRepository.get(interventionId)).thenReturn(titratedDoseIntervention);

    interventionService.setMultipliers(interventionId, command);

    assertEquals(multiplier.getConversionMultiplier(), titratedDoseIntervention.getMinConstraint().getLowerBound().getConversionMultiplier());
    assertNull(titratedDoseIntervention.getMinConstraint().getUpperBound().getConversionMultiplier());
    assertNull(titratedDoseIntervention.getMaxConstraint().getLowerBound().getConversionMultiplier());
    assertEquals(multiplier.getConversionMultiplier(), titratedDoseIntervention.getMaxConstraint().getUpperBound().getConversionMultiplier());
    verify(interventionRepository).get(interventionId);
  }

  @Test
  public void testSetBothDoseTypeIntervention() throws ResourceDoesNotExistException, InvalidConstraintException {
    InterventionMultiplierCommand multiplier = new InterventionMultiplierCommand("milligram",
            URI.create("http://concept.com"), 0.001);
    List<InterventionMultiplierCommand> multipliers = Collections.singletonList(multiplier);
    SetMultipliersCommand command = new SetMultipliersCommand(multipliers);
    LowerBoundCommand minLowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 1., "milligram", "P1D",
            URI.create("http://concept.com"), null);
    UpperBoundCommand minUpperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 5., "nanogram", "P1D",
            URI.create("http://concept.com"), null);
    LowerBoundCommand maxLowerBound = new LowerBoundCommand(LowerBoundType.AT_LEAST, 1., "nanogram", "P1D",
            URI.create("http://concept.com"), null);
    UpperBoundCommand maxUpperBound = new UpperBoundCommand(UpperBoundType.AT_MOST, 5., "milligram", "P1D",
            URI.create("http://concept.com"), null);
    DoseConstraint minConstraint = new DoseConstraint(minLowerBound, minUpperBound);
    DoseConstraint maxConstraint = new DoseConstraint(maxLowerBound, maxUpperBound);
    BothDoseTypesIntervention bothDoseTypesIntervention = new BothDoseTypesIntervention(2, 1, "paraffin", "no motivation",
            URI.create("semanticURI"), "semanticInterventionLabel",
            minConstraint, maxConstraint);
    when(interventionRepository.get(interventionId)).thenReturn(bothDoseTypesIntervention);

    interventionService.setMultipliers(interventionId, command);

    assertEquals(multiplier.getConversionMultiplier(), bothDoseTypesIntervention.getMinConstraint().getLowerBound().getConversionMultiplier());
    assertNull(bothDoseTypesIntervention.getMinConstraint().getUpperBound().getConversionMultiplier());
    assertNull(bothDoseTypesIntervention.getMaxConstraint().getLowerBound().getConversionMultiplier());
    assertEquals(multiplier.getConversionMultiplier(), bothDoseTypesIntervention.getMaxConstraint().getUpperBound().getConversionMultiplier());
    verify(interventionRepository).get(interventionId);
  }
}