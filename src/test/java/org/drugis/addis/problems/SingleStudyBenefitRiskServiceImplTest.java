package org.drugis.addis.problems;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.*;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by daan on 3/27/14.
 */
public class SingleStudyBenefitRiskServiceImplTest {

  @Mock
  private CriterionEntryFactory criterionEntryFactory;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private MappingService mappingService;

  @Mock
  private AnalysisService analysisService;


  @InjectMocks
  private SingleStudyBenefitRiskServiceImpl singleStudyBenefitRiskService;

  private String armName1 = "arm name 1";
  private String armName2 = "arm name 2";
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
  private URI outcomeUri1 = URI.create("outcome1");
  private URI outcomeUri2 = URI.create("outcome2");
  private final SingleStudyContext context = buildContext();

  @Before
  public void setUp() {
    singleStudyBenefitRiskService = new SingleStudyBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(criterionEntryFactory, triplestoreService, mappingService, analysisService);
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
  public void testGetSingleStudyMeasurements() throws ReadValueException, ResourceDoesNotExistException {
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
    List<TrialDataStudy> mockStudies = Collections.singletonList(mockStudy);
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
    URI defaultMeasurementMoment = URI.create("defaultMM");

    TrialDataArm trialDataArm1 = buildTrialDataArm(defaultMeasurementMoment);
    TrialDataArm trialDataArm2 = buildTrialDataArm(defaultMeasurementMoment);
    List<TrialDataArm> arms = Arrays.asList(trialDataArm1, trialDataArm2);

    arms.forEach(arm -> arm.getMeasurementsForMoment(defaultMeasurementMoment)
            .forEach(measurement -> {
              CriterionEntry criterionEntry1 = mock(CriterionEntry.class);
              Outcome measuredOutcome = context.getOutcomesByUri().get(measurement.getVariableConceptUri());
              String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measurement.getVariableConceptUri());
              when(criterionEntryFactory.create(measurement,
                      measuredOutcome.getName(), dataSourceId, context.getSourceLink())).thenReturn(criterionEntry1);
            }));

    // EXECUTE
    Map<URI, CriterionEntry> result = singleStudyBenefitRiskService.getCriteria(arms, defaultMeasurementMoment, context);
    Map<URI, CriterionEntry> expectedResult = new HashMap<>();
    assertEquals(expectedResult, result);

  }

  private TrialDataArm buildTrialDataArm(URI defaultMeasurementMoment) {
    TrialDataArm trialDataArm = mock(TrialDataArm.class);
    Measurement measurement1 = buildMeasurementMock(dichotomousVariable.getVariableConceptUri());
    Measurement measurement2 = buildMeasurementMock(continuousMeasurementStdDev.getVariableConceptUri());
    Set<Measurement> measurements = Sets.newHashSet(measurement1, measurement2);
    when(trialDataArm.getMeasurementsForMoment(defaultMeasurementMoment)).thenReturn(measurements);
    return trialDataArm;
  }

  private Measurement buildMeasurementMock(URI variableConceptUri) {
    Measurement measurement1a = mock(Measurement.class);
    when(measurement1a.getVariableConceptUri()).thenReturn(variableConceptUri);
    return measurement1a;
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
    interventionsById.put(1, mock(AbstractIntervention.class));
    interventionsById.put(2, mock(AbstractIntervention.class));

    Map<URI, String> dataSourcesIdsByOutcomeUri = new HashMap<>();
    dataSourcesIdsByOutcomeUri.put(dichotomousVariable.getVariableConceptUri(), "dataSource1");
    dataSourcesIdsByOutcomeUri.put(continuousMeasurementStdDev.getVariableConceptUri(), "dataSource2");

    URI sourceLink = URI.create("sourceLink");
    return new SingleStudyContext(outcomesByUri, interventionsById, dataSourcesIdsByOutcomeUri, sourceLink);
  }
}
