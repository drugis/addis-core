package org.drugis.addis.problems.service;

import com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.CriterionEntryFactory;
import org.drugis.addis.problems.service.impl.SingleStudyBenefitRiskServiceImpl;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.service.UuidService;
import org.junit.*;
import org.mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

  @Mock
  private ContrastStudyBenefitRiskService contrastStudyBenefitRiskServiceMock;

  @Mock
  private AbsoluteStudyBenefitRiskService absoluteStudyBenefitRiskServiceMock;

  @InjectMocks
  private SingleStudyBenefitRiskServiceImpl singleStudyBenefitRiskService;

  private String armName1 = "arm name 1";
  private final Integer interventionId1 = 1;
  private final Integer interventionId2 = 2;

  private Arm arm1 = new Arm(URI.create("armUri1"), "drugUuid1", armName1);

  private String variableName1 = "variable name 1";
  private String variableName2 = "variable name 2";

  private String studyUuid = "aa-bb";
  private URI studyUri = URI.create(studyUuid);

  private Variable continuousVariable = new Variable(URI.create("continuousUri"), studyUuid, variableName1, "desc", null, false, MeasurementType.RATE, URI.create("dichotomousVarConcept"));
  private Variable dichotomousVariable = new Variable(URI.create("dichotomousUri"), studyUuid, variableName2, "desc", null, false, MeasurementType.CONTINUOUS, URI.create("continuousVarConcept"));

  private Measurement continuousMeasurementStdDev = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
          .setSampleSize(222).setStdDev(0.2).setMean(7.56).build();
  private final SingleStudyContext context = buildContext();
  private final URI defaultMeasurementMoment = URI.create("defaultMM");
  private final String source = "source";

  @Before
  public void setUp() {
    singleStudyBenefitRiskService = new SingleStudyBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(
            criterionEntryFactory,
            triplestoreService,
            mappingService,
            analysisService,
            linkService,
            uuidService);
  }

  @Test
  public void testGetSingleStudyMeasurements() throws ReadValueException, ResourceDoesNotExistException, IOException {
    URI datasetVersion = URI.create("datasetVersion");
    Project project = mock(Project.class);
    URI studyGraphUri = URI.create("studyGraph");
    URI interventionUri1 = URI.create("intervention1");
    String versionedUuid = "versionedUuid";

    Set<AbstractIntervention> interventions = ImmutableSet.copyOf(context.getInterventionsById().values());
    SingleIntervention singleInterventionMock = mock(SingleIntervention.class);

    TrialDataStudy mockStudy = mock(TrialDataStudy.class);
    List<TrialDataStudy> mockStudies = singletonList(mockStudy);

    when(project.getNamespaceUid()).thenReturn("namespaceUuid");
    when(project.getDatasetVersion()).thenReturn(datasetVersion);
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(versionedUuid);
    when(singleInterventionMock.getSemanticInterventionUri()).thenReturn(interventionUri1);
    when(analysisService.getSingleInterventions(interventions)).thenReturn(Sets.newHashSet(singleInterventionMock));
    when(triplestoreService.getSingleStudyData(
            anyString(), any(URI.class), any(URI.class), anySet(), anySet()
    )).thenReturn(mockStudies);

    // EXECUTE
    TrialDataStudy result = singleStudyBenefitRiskService.getStudy(project, studyGraphUri, context);

    assertEquals(mockStudy, result);
    verify(mappingService).getVersionedUuid(project.getNamespaceUid());
    verify(analysisService).getSingleInterventions(interventions);
    verify(triplestoreService).getSingleStudyData(anyString(), any(URI.class), any(URI.class), anySet(), anySet());
  }

  @Test
  public void testGetCriteria() {
    Map<URI, CriterionEntry> expectedResult = new HashMap<>();

    List<TrialDataArm> arms = buildTrialDataArms();

    arms.forEach(arm -> arm.getMeasurementsForMoment(defaultMeasurementMoment)
            .forEach(measurement -> {
              CriterionEntry criterionEntry = mock(CriterionEntry.class);
              URI variableConceptUri = measurement.getVariableConceptUri();
              when(criterionEntryFactory.create(
                      measurement,
                      context)
              ).thenReturn(criterionEntry);

              expectedResult.put(variableConceptUri, criterionEntry);
            }));

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
    verify(criterionEntryFactory).create(measurement, context);
  }

  @Test
  public void testGetAlternatives() {
    List<TrialDataArm> arms = buildTrialDataArms();

    Map<String, AlternativeEntry> result = singleStudyBenefitRiskService.getAlternatives(arms, context);

    Map<String, AlternativeEntry> expectedResult = new HashMap<>();
    expectedResult.put(interventionId1.toString(), new AlternativeEntry(alternative1Name));
    expectedResult.put(interventionId2.toString(), new AlternativeEntry(alternative2Name));
    assertEquals(expectedResult, result);
  }

  private List<TrialDataArm> buildTrialDataArms() {
    TrialDataArm arm1 = buildTrialDataArm(defaultMeasurementMoment, Sets.newHashSet(interventionId1));
    TrialDataArm arm2 = buildTrialDataArm(defaultMeasurementMoment, Sets.newHashSet(interventionId2));
    return Arrays.asList(arm1, arm2);
  }

  private SingleStudyContext buildContext() {
    Outcome mockOutcome1 = mock(Outcome.class);
    Integer outcomeId1 = 1;
    when(mockOutcome1.getSemanticOutcomeUri()).thenReturn(dichotomousVariable.getVariableConceptUri());
    when(mockOutcome1.getName()).thenReturn("outcome1Name");
    when(mockOutcome1.getId()).thenReturn(outcomeId1);

    Map<Integer, AbstractIntervention> interventionsById = new HashMap<>();
    AbstractIntervention interventionMock1 = mock(AbstractIntervention.class);
    when(interventionMock1.getId()).thenReturn(interventionId1);
    when(interventionMock1.getName()).thenReturn(alternative1Name);
    interventionsById.put(interventionId1, interventionMock1);

    AbstractIntervention interventionMock2 = mock(AbstractIntervention.class);
    when(interventionMock2.getId()).thenReturn(interventionId2);
    when(interventionMock2.getName()).thenReturn(alternative2Name);
    interventionsById.put(interventionId2, interventionMock2);

    URI sourceLink = URI.create("sourceLink");

    SingleStudyContext context = new SingleStudyContext();
    context.setSourceLink(sourceLink);
    context.setOutcome(mockOutcome1);
    context.setInterventionsById(interventionsById);
    context.setDataSourceUuid("dataSource1");
    context.setSource(source);

    return context;
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
    List<TrialDataArm> result = singleStudyBenefitRiskService.getMatchedArms(interventions, arms);

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
      singleStudyBenefitRiskService.getMatchedArms(interventions, arms);
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
  public void testBuildContext() throws IOException {
    String uuid1 = "uuid1";

    when(uuidService.generate()).thenReturn(uuid1);

    URI sourceLink = URI.create("sourceLink");
    Project project = mock(Project.class);
    when(linkService.getStudySourceLink(project, studyUri)).thenReturn(sourceLink);

    URI outcome1Uri = URI.create("outcome1Uri");
    Outcome outcome1 = mock(Outcome.class);
    when(outcome1.getSemanticOutcomeUri()).thenReturn(outcome1Uri);
    // Use sorted set to guarantee order so that the uuids are paired correctly

    AbstractIntervention intervention1 = mock(AbstractIntervention.class);
    when(intervention1.getId()).thenReturn(interventionId1);
    AbstractIntervention intervention2 = mock(AbstractIntervention.class);
    when(intervention2.getId()).thenReturn(interventionId2);
    HashSet<AbstractIntervention> interventions = Sets.newHashSet(intervention1, intervention2);

    BenefitRiskStudyOutcomeInclusion inclusion = mock(BenefitRiskStudyOutcomeInclusion.class);

    String datasetUid = "dataset";
    URI datasetVersion = URI.create("bla");
    when(project.getDatasetVersion()).thenReturn(datasetVersion);
    when(project.getNamespaceUid()).thenReturn(datasetUid);

    SingleStudyContext result = singleStudyBenefitRiskService.buildContext(project, studyUri, outcome1, interventions, inclusion, source);

    Map<Integer, AbstractIntervention> interventionsById = new HashMap<>();
    interventionsById.put(interventionId1, intervention1);
    interventionsById.put(interventionId2, intervention2);

    SingleStudyContext expectedResult = new SingleStudyContext();
    expectedResult.setOutcome(outcome1);
    expectedResult.setDataSourceUuid(uuid1);
    expectedResult.setInterventionsById(interventionsById);
    expectedResult.setSourceLink(sourceLink);
    expectedResult.setInclusion(inclusion);
    expectedResult.setSource(source);

    assertEquals(expectedResult, result);

    verify(uuidService).generate();
    verify(linkService).getStudySourceLink(project, studyUri);
  }
}
