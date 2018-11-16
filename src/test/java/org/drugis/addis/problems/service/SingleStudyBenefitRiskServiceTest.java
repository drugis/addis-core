package org.drugis.addis.problems.service;

import com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.CriterionEntryFactory;
import org.drugis.addis.problems.service.impl.SingleStudyBenefitRiskServiceImpl;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.MeasurementBuilder;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.trialverse.util.service.UuidService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/27/14.
 */
public class SingleStudyBenefitRiskServiceTest {

  private final String alternative1Name = "alternative1Name";
  private final String alternative2Name = "alternative2Name";
  @Mock
  private CriterionEntryFactory criterionEntryFactory;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private MappingService mappingService;

  @Mock
  private AnalysisService analysisService;

  @Mock
  private LinkService linkService;

  @Mock
  private UuidService uuidService;

  @InjectMocks
  private SingleStudyBenefitRiskServiceImpl singleStudyBenefitRiskService;

  private String armName1 = "arm name 1";
  private String armName2 = "arm name 2";
  private final Integer interventionId1 = 1;
  private final Integer interventionId2 = 2;

  private Arm arm1 = new Arm(URI.create("armUri1"), "drugUuid1", armName1);
  private Arm arm2 = new Arm(URI.create("armUri2"), "drugUuid2", armName2);

  private URI criterionUri1 = URI.create("critUri1");
  private URI criterionUri2 = URI.create("critUri2");

  private String variableName1 = "variable name 1";
  private String variableName2 = "variable name 2";

  private String studyUuid = "aa-bb";
  private URI studyUri = URI.create(studyUuid);
  private String dataSourceUuid = "dataSource1";

  private Variable continuousVariable = new Variable(URI.create("continuousUri"), studyUuid, variableName1, "desc", null, false, MeasurementType.RATE, URI.create("dichotomousVarConcept"));
  private Variable dichotomousVariable = new Variable(URI.create("dichotomousUri"), studyUuid, variableName2, "desc", null, false, MeasurementType.CONTINUOUS, URI.create("continuousVarConcept"));

  private Measurement dichotomousMeasurement = new MeasurementBuilder(studyUri, dichotomousVariable.getUri(), dichotomousVariable.getVariableConceptUri(), arm1.getUri(), DICHOTOMOUS_TYPE_URI)
          .setSampleSize(111).setRate(42).createMeasurement();
  private Measurement continuousMeasurementStdDev = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
          .setSampleSize(222).setStdDev(0.2).setMean(7.56).createMeasurement();
  private Measurement continuousMeasurementStdErr = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
          .setSampleSize(333).setStdErr(0.3).setMean(7.56).createMeasurement();
  private final SingleStudyContext context = buildContext();
  private final URI defaultMeasurementMoment = URI.create("defaultMM");

  @Before
  public void setUp() {
    singleStudyBenefitRiskService = new SingleStudyBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(criterionEntryFactory, triplestoreService, mappingService,
        analysisService, linkService,
        uuidService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBuildPerformanceTable() {

    Integer rate = dichotomousMeasurement.getRate();

    Double mu = continuousMeasurementStdDev.getMean();
    Double stdDev = continuousMeasurementStdDev.getStdDev();
    Double stdErr = continuousMeasurementStdErr.getStdErr();

    URI studyUri = URI.create("itsastudio");
    Integer alternativeId1 = 1;
    Integer alternativeId2 = 2;

    MeasurementBuilder continuousStdDevBuilder = new MeasurementBuilder(studyUri, criterionUri1, continuousMeasurementStdDev.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
            .setSampleSize(continuousMeasurementStdDev.getSampleSize())
            .setStdDev(stdDev)
            .setMean(mu);
    MeasurementBuilder dichotomousBuilder = new MeasurementBuilder(studyUri, criterionUri2, dichotomousMeasurement.getVariableConceptUri(), arm2.getUri(), DICHOTOMOUS_TYPE_URI)
            .setSampleSize(dichotomousMeasurement.getSampleSize())
            .setRate(rate);

    Measurement arm1ContinuousStdDev = continuousStdDevBuilder.createMeasurement();
    continuousStdDevBuilder.setArmUri(arm2.getUri());
    Measurement arm2ContinuousStdDev = continuousStdDevBuilder.createMeasurement();

    MeasurementWithCoordinates continuousStdDevRow1 = new MeasurementWithCoordinates(arm1ContinuousStdDev, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates continuousStdDevRow2 = new MeasurementWithCoordinates(arm2ContinuousStdDev, alternativeId2, dataSourceUuid);

    Measurement arm1Dichotomous = dichotomousBuilder.createMeasurement();
    dichotomousBuilder.setArmUri(arm2.getUri());
    Measurement arm2Dichotomous = dichotomousBuilder.createMeasurement();
    MeasurementWithCoordinates dichotomousRow1 = new MeasurementWithCoordinates(arm1Dichotomous, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates dichotomousRow2 = new MeasurementWithCoordinates(arm2Dichotomous, alternativeId2, dataSourceUuid);

    MeasurementBuilder continuousStdErrBuilder = new MeasurementBuilder(studyUri, criterionUri1, continuousMeasurementStdErr.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
            .setSampleSize(continuousMeasurementStdErr.getSampleSize())
            .setStdErr(stdErr);
    Measurement arm1ContinuousStdErr = continuousStdErrBuilder.createMeasurement();
    continuousStdErrBuilder.setArmUri(arm2.getUri());
    Measurement arm2ContinuousStdErr = continuousStdErrBuilder.createMeasurement();
    MeasurementWithCoordinates continuousStdErrRow1 = new MeasurementWithCoordinates(arm1ContinuousStdErr, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates continuousStdErrRow2 = new MeasurementWithCoordinates(arm2ContinuousStdErr, alternativeId2, dataSourceUuid);

    Set<MeasurementWithCoordinates> measurementsWithCoordinates = ImmutableSet.of(continuousStdDevRow1, continuousStdDevRow2, dichotomousRow1, dichotomousRow2, continuousStdErrRow1, continuousStdErrRow2);

    // EXECUTE
    List<AbstractMeasurementEntry> performanceTable = singleStudyBenefitRiskService.buildPerformanceTable(measurementsWithCoordinates);

    // ASSERTS
    assertEquals(6, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeId1.toString(), continuousMeasurementEntry.getAlternative());
    assertEquals(continuousVariable.getVariableConceptUri().toString(), continuousMeasurementEntry.getCriterion());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
    assertEquals(dataSourceUuid, continuousMeasurementEntry.getDataSource());

    Double expectedMu = continuousMeasurementStdDev.getMean();
    Integer expectedSampleSize = continuousMeasurementStdDev.getSampleSize();
    Double expectedSigma = continuousMeasurementStdDev.getStdDev() / Math.sqrt(expectedSampleSize);

    ContinuousPerformanceParameters parameters = continuousMeasurementEntry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(2);
    assertEquals(alternativeId1.toString(), rateMeasurementEntry.getAlternative());
    assertEquals(dichotomousVariable.getVariableConceptUri().toString(), rateMeasurementEntry.getCriterion());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Integer expectedAlpha = dichotomousMeasurement.getRate() + 1;
    Integer expectedBeta = dichotomousMeasurement.getSampleSize() - dichotomousMeasurement.getRate() + 1;
    assertEquals(expectedAlpha, rateMeasurementEntry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, rateMeasurementEntry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    ContinuousMeasurementEntry stdErrBasedEntry = (ContinuousMeasurementEntry) performanceTable.get(4);
    assertEquals(ContinuousPerformance.DNORM, stdErrBasedEntry.getPerformance().getType());
    assertEquals(continuousMeasurementStdErr.getStdErr(), stdErrBasedEntry.getPerformance().getParameters().getSigma());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownMeasurementTypeThrows() {
    Integer alternativeId1 = 1;
    URI UNKNOWN_TYPE = URI.create("unknown");
    MeasurementBuilder unknownBuilder = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), UNKNOWN_TYPE);
    Measurement arm1Unknown = unknownBuilder.createMeasurement();
    MeasurementWithCoordinates unknownRow1 = new MeasurementWithCoordinates(arm1Unknown, alternativeId1, dataSourceUuid);
    Set<MeasurementWithCoordinates> measurementsWithUnknownType = ImmutableSet.of(unknownRow1);
    singleStudyBenefitRiskService.buildPerformanceTable(measurementsWithUnknownType);
  }


  @Test
  public void testGetSingleStudyMeasurements() throws ReadValueException, ResourceDoesNotExistException, IOException {
    URI datasetVersion = URI.create("datasetVersion");
    Project project = mock(Project.class);
    when(project.getNamespaceUid()).thenReturn("namespaceUuid");
    when(project.getDatasetVersion()).thenReturn(datasetVersion);
    URI studyGraphUri = URI.create("studyGraph");

    URI interventionUri1 = URI.create("intervention1");

    String versionedUuid = "versionedUuid";
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(versionedUuid);

    Set<AbstractIntervention> interventions = ImmutableSet.copyOf(context.getInterventionsById().values());
    SingleIntervention singleInterventionMock = mock(SingleIntervention.class);
    when(singleInterventionMock.getSemanticInterventionUri()).thenReturn(interventionUri1);
    when(analysisService.getSingleInterventions(interventions)).thenReturn(Sets.newHashSet(singleInterventionMock));

    Set<URI> outcomeUris = Sets.newHashSet(dichotomousVariable.getVariableConceptUri(), continuousVariable.getVariableConceptUri());
    Set<URI> interventionUris = Sets.newHashSet(interventionUri1);
    TrialDataStudy mockStudy = mock(TrialDataStudy.class);
    List<TrialDataStudy> mockStudies = singletonList(mockStudy);
    when(triplestoreService.getSingleStudyData(versionedUuid, studyGraphUri, datasetVersion, outcomeUris, interventionUris))
            .thenReturn(mockStudies);

    // EXECUTE
    TrialDataStudy result = singleStudyBenefitRiskService.getSingleStudyMeasurements(project, studyGraphUri, context);

    assertEquals(mockStudy, result);
    verify(mappingService).getVersionedUuid(project.getNamespaceUid());
    verify(analysisService).getSingleInterventions(interventions);
    verify(triplestoreService).getSingleStudyData(versionedUuid, studyGraphUri, datasetVersion, outcomeUris, interventionUris);
  }

  @Test
  public void testGetCriteria() {
    Map<URI, CriterionEntry> expectedResult = new HashMap<>();

    List<TrialDataArm> arms = buildTrialDataArms();

    arms.forEach(arm -> arm.getMeasurementsForMoment(defaultMeasurementMoment)
            .forEach(measurement -> {
              CriterionEntry criterionEntry = mock(CriterionEntry.class);
              URI variableConceptUri = measurement.getVariableConceptUri();
              Outcome measuredOutcome = context.getOutcomesByUri().get(variableConceptUri);
              String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measurement.getVariableConceptUri());
              when(criterionEntryFactory.create(measurement,
                      measuredOutcome.getName(), dataSourceId, context.getSourceLink())).thenReturn(criterionEntry);

              expectedResult.put(variableConceptUri, criterionEntry);
            }));

    // EXECUTE
    Map<URI, CriterionEntry> result = singleStudyBenefitRiskService.getCriteria(arms, defaultMeasurementMoment, context);
    assertEquals(expectedResult, result);

    arms.forEach((arm -> arm.getMeasurementsForMoment(defaultMeasurementMoment)
            .forEach(this::verifyCriterionCreation)
    ));
  }

  private TrialDataArm buildTrialDataArm(URI defaultMeasurementMoment, Set<Integer> matchedInterventionIds) {
    TrialDataArm trialDataArm = mock(TrialDataArm.class);
    Measurement measurement1 = buildMeasurementMock(dichotomousVariable.getVariableConceptUri());
    Measurement measurement2 = buildMeasurementMock(continuousMeasurementStdDev.getVariableConceptUri());
    Set<Measurement> measurements = Sets.newHashSet(measurement1, measurement2);
    when(trialDataArm.getMeasurementsForMoment(defaultMeasurementMoment)).thenReturn(measurements);
    when(trialDataArm.getMatchedProjectInterventionIds()).thenReturn(matchedInterventionIds);
    return trialDataArm;
  }

  private Measurement buildMeasurementMock(URI variableConceptUri) {
    Measurement measurement1a = mock(Measurement.class);
    when(measurement1a.getVariableConceptUri()).thenReturn(variableConceptUri);
    return measurement1a;
  }

  private void verifyCriterionCreation(Measurement measurement) {
    URI variableConceptUri = measurement.getVariableConceptUri();
    Outcome measuredOutcome = context.getOutcomesByUri().get(variableConceptUri);
    String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measuredOutcome.getSemanticOutcomeUri());
    verify(criterionEntryFactory).create(measurement, measuredOutcome.getName(), dataSourceId, context.getSourceLink());
  }

  @Test
  public void testGetAlternatives() {
    List<TrialDataArm> arms = buildTrialDataArms();

    // exEcUte
    Map<String, AlternativeEntry> result = singleStudyBenefitRiskService.getAlternatives(arms, context);

    Map<String, AlternativeEntry> expectedResult = new HashMap<>();
    expectedResult.put(interventionId1.toString(), new AlternativeEntry(interventionId1, alternative1Name));
    expectedResult.put(interventionId2.toString(), new AlternativeEntry(interventionId2, alternative2Name));
    assertEquals(expectedResult, result);
  }

  private List<TrialDataArm> buildTrialDataArms() {
    TrialDataArm arm1 = buildTrialDataArm(defaultMeasurementMoment, Sets.newHashSet(interventionId1));
    TrialDataArm arm2 = buildTrialDataArm(defaultMeasurementMoment, Sets.newHashSet(interventionId2));
    return Arrays.asList(arm1, arm2);
  }


  @Test
  public void testGetMeasurementsWithCoordinates() {
    List<TrialDataArm> arms = buildTrialDataArms();

    // Executor
    Set<MeasurementWithCoordinates> result = singleStudyBenefitRiskService.getMeasurementsWithCoordinates(arms, defaultMeasurementMoment, context);

    Set<MeasurementWithCoordinates> expectedResult = arms.stream().
            map(arm -> arm.getMeasurementsForMoment(defaultMeasurementMoment).stream()
                    .map(measurement -> buildMeasurementWithCoordinates(arm, measurement))
                    .collect(Collectors.toSet()))
            .reduce(new HashSet<>(), Sets::union);
    assertEquals(expectedResult, result);
  }

  private MeasurementWithCoordinates buildMeasurementWithCoordinates(TrialDataArm arm, Measurement measurement) {
    Integer interventionId = arm.getMatchedProjectInterventionIds().iterator().next();
    Outcome measuredOutcome = context.getOutcomesByUri().get(measurement.getVariableConceptUri());
    String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measuredOutcome.getSemanticOutcomeUri());
    return new MeasurementWithCoordinates(measurement, interventionId, dataSourceId);
  }

  private SingleStudyContext buildContext() {
    Map<URI, Outcome> outcomesByUri = new HashMap<>();

    Outcome mockOutcome1 = mock(Outcome.class);
    when(mockOutcome1.getSemanticOutcomeUri()).thenReturn(dichotomousVariable.getVariableConceptUri());
    when(mockOutcome1.getName()).thenReturn("outcome1Name");
    outcomesByUri.put(dichotomousVariable.getVariableConceptUri(), mockOutcome1);

    Outcome mockOutcome2 = mock(Outcome.class);
    when(mockOutcome2.getSemanticOutcomeUri()).thenReturn(continuousMeasurementStdDev.getVariableConceptUri());
    when(mockOutcome2.getName()).thenReturn("outcome2Name");
    outcomesByUri.put(continuousMeasurementStdDev.getVariableConceptUri(), mockOutcome2);

    Map<Integer, AbstractIntervention> interventionsById = new HashMap<>();
    AbstractIntervention interventionMock1 = mock(AbstractIntervention.class);
    when(interventionMock1.getId()).thenReturn(interventionId1);
    when(interventionMock1.getName()).thenReturn(alternative1Name);
    interventionsById.put(interventionId1, interventionMock1);

    AbstractIntervention interventionMock2 = mock(AbstractIntervention.class);
    when(interventionMock2.getId()).thenReturn(interventionId2);
    when(interventionMock2.getName()).thenReturn(alternative2Name);
    interventionsById.put(interventionId2, interventionMock2);

    Map<URI, String> dataSourcesIdsByOutcomeUri = new HashMap<>();
    dataSourcesIdsByOutcomeUri.put(dichotomousVariable.getVariableConceptUri(), "dataSource1");
    dataSourcesIdsByOutcomeUri.put(continuousMeasurementStdDev.getVariableConceptUri(), "dataSource2");

    URI sourceLink = URI.create("sourceLink");
    return new SingleStudyContext(outcomesByUri, interventionsById, dataSourcesIdsByOutcomeUri, sourceLink);
  }

  @Test
  public void testGetArmsWithMatching() {
    AbstractIntervention intervention1 = mock(AbstractIntervention.class);
    when(intervention1.getId()).thenReturn(interventionId1);
    AbstractIntervention intervention2 = mock(AbstractIntervention.class);
    when(intervention2.getId()).thenReturn(interventionId2);
    HashSet<AbstractIntervention> interventions = Sets.newHashSet(intervention1, intervention2);

    TrialDataArm arm1 = buildTrialDataArm(defaultMeasurementMoment, singleton(interventionId1));
    TrialDataArm arm2 = buildTrialDataArm(defaultMeasurementMoment, singleton(interventionId2));
    TrialDataArm unMatchedArm = buildTrialDataArm(defaultMeasurementMoment, emptySet());
    List<TrialDataArm> arms = Arrays.asList(arm1, arm2, unMatchedArm);

    when(triplestoreService.findMatchingIncludedInterventions(interventions, arm1)).thenReturn(singleton(intervention1));
    when(triplestoreService.findMatchingIncludedInterventions(interventions, arm2)).thenReturn(singleton(intervention2));
    when(triplestoreService.findMatchingIncludedInterventions(interventions, unMatchedArm)).thenReturn(emptySet());

    // execute
    List<TrialDataArm> result = singleStudyBenefitRiskService.getArmsWithMatching(interventions, arms);

    List<TrialDataArm> matchedArms = Arrays.asList(arm1, arm2);
    assertEquals(matchedArms, result);

    verify(arm1).setMatchedProjectInterventionIds(singleton(interventionId1));
    verify(arm2).setMatchedProjectInterventionIds(singleton(interventionId2));
    verify(unMatchedArm).setMatchedProjectInterventionIds(emptySet());

    verify(triplestoreService).findMatchingIncludedInterventions(interventions, arm1);
    verify(triplestoreService).findMatchingIncludedInterventions(interventions, arm2);
    verify(triplestoreService).findMatchingIncludedInterventions(interventions, unMatchedArm);
  }

  @Test
  public void testGetArmsWithMatchingTooManyMatchesThrows() {
    Set<AbstractIntervention> interventions = buildInterventions();

    TrialDataArm arm1 = buildTrialDataArm(defaultMeasurementMoment, Sets.newHashSet(interventionId1));
    List<TrialDataArm> arms = singletonList(arm1);
    Set<AbstractIntervention> matches = emptySet(); // doesn't matter, because arm is mock

    when(triplestoreService.findMatchingIncludedInterventions(interventions, arm1)).thenReturn(matches);
    when(arm1.getMatchedProjectInterventionIds()).thenReturn(Sets.newHashSet(interventionId1, interventionId2));

    Boolean thrown = false;
    try {
      singleStudyBenefitRiskService.getArmsWithMatching(interventions, arms);
    } catch (RuntimeException e) {
      assertEquals("too many matched interventions for arm when creating problem", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
    verify(triplestoreService).findMatchingIncludedInterventions(interventions, arm1);
  }

  private Set<AbstractIntervention> buildInterventions() {
    AbstractIntervention intervention1 = mock(AbstractIntervention.class);
    when(intervention1.getId()).thenReturn(interventionId1);
    AbstractIntervention intervention2 = mock(AbstractIntervention.class);
    when(intervention2.getId()).thenReturn(interventionId2);
    return Sets.newHashSet(intervention1, intervention2);
  }

  @Test
  public void testBuildContext() {
    String uuid1 = "uuid1";
    String uuid2 = "uuid2";
    when(uuidService.generate()).thenReturn(uuid1, uuid2);

    URI sourceLink = URI.create("sourceLink");
    Project project = mock(Project.class);
    when(linkService.getStudySourceLink(project, studyUri)).thenReturn(sourceLink);

    URI outcome1Uri = URI.create("outcome1Uri");
    URI outcome2Uri = URI.create("outcome2Uri");
    Outcome outcome1 = mock(Outcome.class);
    when(outcome1.getSemanticOutcomeUri()).thenReturn(outcome1Uri);
    Outcome outcome2 = mock(Outcome.class);
    when(outcome2.getSemanticOutcomeUri()).thenReturn(outcome2Uri);
    // Use sorted set to guarantee order so that the uuids are paired correctly
    Set<Outcome> outcomes = new TreeSet<>(Comparator.comparing(Outcome::getSemanticOutcomeUri));
    outcomes.addAll(Sets.newHashSet(outcome1, outcome2));
    AbstractIntervention intervention1 = mock(AbstractIntervention.class);
    when(intervention1.getId()).thenReturn(interventionId1);
    AbstractIntervention intervention2 = mock(AbstractIntervention.class);
    when(intervention2.getId()).thenReturn(interventionId2);
    HashSet<AbstractIntervention> interventions = Sets.newHashSet(intervention1, intervention2);

    SingleStudyContext result = singleStudyBenefitRiskService.buildContext(project, studyUri, outcomes, interventions);

    Map<URI, Outcome> outcomesByUri = new HashMap<>();
    outcomesByUri.put(outcome1Uri, outcome1);
    outcomesByUri.put(outcome2Uri, outcome2);

    Map<Integer, AbstractIntervention> interventionsById = new HashMap<>();
    interventionsById.put(interventionId1, intervention1);
    interventionsById.put(interventionId2, intervention2);
    Map<URI, String> dataSourceIdsByOutcome = new HashMap<>();
    dataSourceIdsByOutcome.put(outcome1Uri, uuid1);
    dataSourceIdsByOutcome.put(outcome2Uri, uuid2);
    SingleStudyContext expectedResult = new SingleStudyContext(outcomesByUri, interventionsById, dataSourceIdsByOutcome, sourceLink);

    System.out.println(expectedResult.getOutcomesByUri());
    System.out.println(result.getOutcomesByUri());
    System.out.println(expectedResult.getInterventionsById());
    System.out.println(result.getInterventionsById());
    System.out.println(expectedResult.getDataSourceIdsByOutcomeUri());
    System.out.println(result.getDataSourceIdsByOutcomeUri());
    System.out.println(expectedResult.getSourceLink());
    System.out.println(result.getSourceLink());


    assertEquals(expectedResult, result);

    verify(uuidService, times(2)).generate();
    verify(linkService).getStudySourceLink(project, studyUri);
  }
}
