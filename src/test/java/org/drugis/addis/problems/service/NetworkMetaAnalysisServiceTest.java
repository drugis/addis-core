package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.NetworkMetaAnalysisEntryBuilder;
import org.drugis.addis.problems.service.impl.NetworkMetaAnalysisServiceImpl;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.trialverse.util.service.UuidService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class NetworkMetaAnalysisServiceTest {

  @Mock
  private AnalysisService analysisService;

  @Mock
  private CovariateRepository covariateRepository;

  @Mock
  private NetworkMetaAnalysisEntryBuilder networkMetaAnalysisEntryBuilder;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @Mock
  private UuidService uuidService;

  @InjectMocks
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  private final Integer projectId = 101;
  private final Integer analysisId = 202;
  private final String projectDatasetUid = "projectDatasetUid";
  private final URI projectDatasetVersion = URI.create("http://versions.com/versions/versionedUuid");

  private final Account owner = mock(Account.class);
  private final Project project = new Project(projectId, owner, "project name", "desc", projectDatasetUid, projectDatasetVersion);

  private final SemanticVariable semanticOutcome = new SemanticVariable(URI.create("semanticOutcomeUri"), "semanticOutcomeLabel");
  private Integer direction = 1;
  private final Outcome outcome = new Outcome(303, project.getId(), "outcome name", direction, "moti", semanticOutcome);

  private final URI fluoxConceptUri = URI.create("fluoxConceptUri");
  private final SemanticInterventionUriAndName fluoxConcept = new SemanticInterventionUriAndName(fluoxConceptUri, "fluox concept");
  private final Integer fluoxInterventionId = 401;
  private final SingleIntervention fluoxIntervention = new SimpleIntervention(fluoxInterventionId, project.getId(),
          "fluoxetine", "moti", fluoxConcept.getUri(), fluoxConcept.getLabel());
  private final URI paroxConceptUri = URI.create("paroxConceptUri");
  private final SemanticInterventionUriAndName paroxConcept = new SemanticInterventionUriAndName(paroxConceptUri, "parox concept");
  private final Integer paroxInterventionId = 402;
  private final SingleIntervention paroxIntervention = new SimpleIntervention(paroxInterventionId, project.getId(),
          "paroxetine", "moti", paroxConcept.getUri(), paroxConcept.getLabel());
  private final URI sertraConceptUri = URI.create("sertraConceptUri");
  private final SemanticInterventionUriAndName sertraConcept = new SemanticInterventionUriAndName(sertraConceptUri, "sertra concept");
  private final Integer sertraInterventionId = 403;
  private final SingleIntervention sertraIntervention = new SimpleIntervention(sertraInterventionId, project.getId(),
          "sertraline", "moti", sertraConcept.getUri(), sertraConcept.getLabel());

  // empty constructor so exception from field initialisation can go somewhere
  public NetworkMetaAnalysisServiceTest() throws Exception {
  }


  @Before
  public void setUp() {
    networkMetaAnalysisService = new NetworkMetaAnalysisServiceImpl();
    initMocks(this);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(analysisService, covariateRepository, networkMetaAnalysisEntryBuilder, pataviTaskRepository, uuidService);
  }

  @Test
  public void testGetTreatments() throws ResourceDoesNotExistException {
    NetworkMetaAnalysis analysis = mock(NetworkMetaAnalysis.class);

    AbstractIntervention intervention = mock(SimpleIntervention.class);
    Integer interventionId = 1;
    when(intervention.getId()).thenReturn(interventionId);
    String interventionName = "interventionName";
    when(intervention.getName()).thenReturn(interventionName);

    Set<AbstractIntervention> includedInterventions = Sets.newHashSet(intervention);
    when(analysisService.getIncludedInterventions(analysis)).thenReturn(includedInterventions);

    TreatmentEntry treatment1 = new TreatmentEntry(interventionId, interventionName);
    List<TreatmentEntry> expectedResult = Lists.newArrayList(treatment1);

    // eggsecute
    List<TreatmentEntry> results = networkMetaAnalysisService.getTreatments(analysis);

    assertEquals(expectedResult, results);
    verify(analysisService).getIncludedInterventions(analysis);
  }

  @Test
  public void testBuildPerformanceEntries() {
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);

    // add study with excluded arms to check whether it's excluded from covariate entries
    String tooFewStudyName = "DaanEtAlUri";
    URI daanEtAlUri = URI.create(tooFewStudyName);

    URI tooFewArmUri1 = URI.create("tooFewArm1");
    Set<Integer> matchedInterventionIds = Sets.newHashSet(fluoxInterventionId);
    TrialDataArm matchedArm = buildArmMock(tooFewArmUri1, matchedInterventionIds, Collections.emptySet());
    URI tooFewArmUri2 = URI.create("tooFewArm2");
    TrialDataArm unmatchedArm = buildArmMock(tooFewArmUri2, Collections.emptySet(), Collections.emptySet());
    URI tooFewArmUri3 = URI.create("tooFewArm3");
    TrialDataArm excludedArm = buildArmMock(tooFewArmUri3, Collections.emptySet(), Collections.emptySet());
    URI tooFewArmUri4 = URI.create("tooFewArm4");
    TrialDataArm noMeasurementsArm = buildArmMock(tooFewArmUri4, Collections.emptySet(), null);

    List<TrialDataArm> tooFewValidArmsStudy = Arrays.asList(
            matchedArm,
            unmatchedArm,
            excludedArm,
            noMeasurementsArm);
    TrialDataStudy studyWithTooFewArms = new TrialDataStudy(
            daanEtAlUri,
            tooFewStudyName,
            tooFewValidArmsStudy);

    networkMetaAnalysis.updateArmExclusions(Sets.newHashSet(new ArmExclusion(analysisId, tooFewArmUri3)));

    // add valid study with non-default measurement moment
    String nonDefaultStudyName = "nonDefaultStudyName";
    URI studyUri = URI.create(nonDefaultStudyName);
    URI nonDefaultMeasurementMomentUri = URI.create("nonDefaultMeasurementMoment");
    URI defaultMeasurementMomentUri = URI.create("defaultMeasurementMoment");

    URI nonDefaultArmUri1 = URI.create("nonDefaultArmUri1");
    Set<Integer> matchedInterventionIds1 = Sets.newHashSet(fluoxInterventionId);
    Measurement fluoxMeasurement = mock(Measurement.class);
    Set<Measurement> measurements1 = Sets.newHashSet(fluoxMeasurement);
    TrialDataArm defaultMMArm = buildArmMock(
            nonDefaultArmUri1,
            matchedInterventionIds1,
            measurements1,
            nonDefaultMeasurementMomentUri);

    URI nonDefaultArmUri2 = URI.create("nonDefaultArmUri2");
    Set<Integer> matchedInterventionIds2 = Sets.newHashSet(paroxInterventionId);
    Measurement paroxMeasurement = mock(Measurement.class);
    Set<Measurement> measurements2 = Sets.newHashSet(paroxMeasurement);
    TrialDataArm nonDefaultMMArm = buildArmMock(
            nonDefaultArmUri2,
            matchedInterventionIds2,
            measurements2,
            nonDefaultMeasurementMomentUri);

    List<TrialDataArm> nonDefaultArms = Arrays.asList(defaultMMArm, nonDefaultMMArm);
    TrialDataStudy studyWithNonDefaultMeasurementMoment = new TrialDataStudy(studyUri, nonDefaultStudyName, nonDefaultArms);

    // add normal study
    String normalStudyName = "normalStudy";
    URI normalStudyUri = URI.create(normalStudyName);

    URI normalArmUri1 = URI.create("normalArmUri1");
    TrialDataArm normalArm1 = buildArmMock(
            normalArmUri1,
            matchedInterventionIds1,
            measurements1);

    URI normalArmUri2 = URI.create("normalArmUri2");
    TrialDataArm normalArm2 = buildArmMock(
            normalArmUri2,
            matchedInterventionIds2,
            measurements2);

    List<TrialDataArm> normalArms = Arrays.asList(normalArm1, normalArm2);
    TrialDataStudy normalStudy = new TrialDataStudy(normalStudyUri, normalStudyName, normalArms);

    // create study list
    List<TrialDataStudy> trialDataStudies = Arrays.asList(studyWithTooFewArms, studyWithNonDefaultMeasurementMoment, normalStudy);
    networkMetaAnalysis.updateIncludedMeasurementMoments(Sets.newHashSet(
            new MeasurementMomentInclusion(
                    analysisId,
                    studyWithNonDefaultMeasurementMoment.getStudyUri(),
                    nonDefaultMeasurementMomentUri)
            , new MeasurementMomentInclusion(
                    analysisId,
                    normalStudy.getStudyUri(),
                    defaultMeasurementMomentUri))
    );

    AbstractNetworkMetaAnalysisProblemEntry fluoxEntry = mock(ContinuousStdErrEntry.class);
    AbstractNetworkMetaAnalysisProblemEntry paroxEntry = new ContinuousNetworkMetaAnalysisProblemEntry("study", 1, 2, 3.0, 4.0);
    when(networkMetaAnalysisEntryBuilder.build(normalStudyName, fluoxInterventionId, fluoxMeasurement)).thenReturn(fluoxEntry);
    when(networkMetaAnalysisEntryBuilder.build(normalStudyName, paroxInterventionId, paroxMeasurement)).thenReturn(paroxEntry);
    when(networkMetaAnalysisEntryBuilder.build(nonDefaultStudyName, fluoxInterventionId, fluoxMeasurement)).thenReturn(fluoxEntry);
    when(networkMetaAnalysisEntryBuilder.build(nonDefaultStudyName, paroxInterventionId, paroxMeasurement)).thenReturn(paroxEntry);

    // eggsecutor
    List<AbstractNetworkMetaAnalysisProblemEntry> results = networkMetaAnalysisService.buildPerformanceEntries(networkMetaAnalysis, trialDataStudies);

    Double stdErr = 4.0 / Math.sqrt(2.0);
    AbstractNetworkMetaAnalysisProblemEntry stdErrParoxEntry = new ContinuousStdErrEntry("study", 1, 3.0, stdErr);
    List<AbstractNetworkMetaAnalysisProblemEntry> expectedResult = Arrays.asList(fluoxEntry, stdErrParoxEntry, fluoxEntry, stdErrParoxEntry);
    assertEquals(expectedResult, results);

    verify(networkMetaAnalysisEntryBuilder).build(normalStudyName, fluoxInterventionId, fluoxMeasurement);
    verify(networkMetaAnalysisEntryBuilder).build(normalStudyName, paroxInterventionId, paroxMeasurement);
    verify(networkMetaAnalysisEntryBuilder).build(nonDefaultStudyName, fluoxInterventionId, fluoxMeasurement);
    verify(networkMetaAnalysisEntryBuilder).build(nonDefaultStudyName, paroxInterventionId, paroxMeasurement);
  }

  private TrialDataArm buildArmMock(URI armUri, Set<Integer> matchedInterventionIds, Set<Measurement> measurements) {
    return buildArmMock(armUri, matchedInterventionIds, measurements, null);
  }

  private TrialDataArm buildArmMock(URI armUri, Set<Integer> matchedInterventionIds, Set<Measurement> measurements, URI nonDefaultMeasurementMoment) {
    URI measurementMoment = (nonDefaultMeasurementMoment == null) ? URI.create("defaultMeasurementMoment") : nonDefaultMeasurementMoment;
    TrialDataArm arm = mock(TrialDataArm.class);
    when(arm.getMatchedProjectInterventionIds()).thenReturn(matchedInterventionIds);
    when(arm.getUri()).thenReturn(armUri);
    when(arm.getMeasurementsForMoment(measurementMoment)).thenReturn(measurements);
    return arm;
  }

  @Test
  public void testGetStudiesWithEntries() {
    URI someStudyUri = URI.create("someURi.com");

    String studyWithEntriesName = "studyWithEntries";
    TrialDataStudy studyWithEntries = new TrialDataStudy(someStudyUri, studyWithEntriesName, null);
    TrialDataStudy studyWithoutEntries = new TrialDataStudy(someStudyUri, "studyWithoutEntries", null);
    List<TrialDataStudy> studies = Arrays.asList(studyWithEntries, studyWithoutEntries);

    AbstractNetworkMetaAnalysisProblemEntry entry = mock(AbstractNetworkMetaAnalysisProblemEntry.class);
    when(entry.getStudy()).thenReturn(studyWithEntriesName);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = singletonList(entry);

    List<TrialDataStudy> expectedResult = singletonList(studyWithEntries);

    //issocute
    List<TrialDataStudy> result = networkMetaAnalysisService.getStudiesWithEntries(studies, entries);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testGetCovariatesIfNoInclusions() {
    NetworkMetaAnalysis analysis = mock(NetworkMetaAnalysis.class);
    when(analysis.getCovariateInclusions()).thenReturn(Collections.emptyList());
    List<TrialDataStudy> studies = singletonList(mock(TrialDataStudy.class));

    // overconfidence is a slow and insidious killer
    Map<String, Map<String, Double>> result = networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studies);
    assertNull(result);
  }

  @Test
  public void getStudyLevelCovariates() {
    String analysisTitle = "analysis";
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, analysisTitle);
    Integer includedCovariateId = 1;
    Integer excludedCovariateId = -1;
    CovariateInclusion inclusion = new CovariateInclusion(analysisId, includedCovariateId);
    List<CovariateInclusion> covariateInclusions = singletonList(inclusion);
    analysis.updateCovariateInclusions(covariateInclusions);

    String includedKey = "includedKey";
    URI studyUri = URI.create("someRandomURI");
    CovariateStudyValue includedCovariateStudyValue = new CovariateStudyValue(studyUri, includedKey, -300.);
    CovariateStudyValue excludedCovariateStudyValue = new CovariateStudyValue(studyUri, "excludedKey", 300.);
    List<CovariateStudyValue> covariateStudyValues = Arrays.asList(includedCovariateStudyValue, excludedCovariateStudyValue);

    TrialDataStudy study = mock(TrialDataStudy.class);
    String studyName = "study";
    when(study.getName()).thenReturn(studyName);
    when(study.getCovariateValues()).thenReturn(covariateStudyValues);
    List<TrialDataStudy> studies = singletonList(study);

    Covariate includedCovariate= mock(Covariate.class);
    Covariate excludedCovariate = mock(Covariate.class);
    when(includedCovariate.getId()).thenReturn(includedCovariateId);
    when(excludedCovariate.getId()).thenReturn(excludedCovariateId);
    when(includedCovariate.getDefinitionKey()).thenReturn(includedKey);
    String includedCovariateName = "includedName";
    when(includedCovariate.getName()).thenReturn(includedCovariateName);

    Collection<Covariate> projectCovariates = Arrays.asList(includedCovariate, excludedCovariate);
    when(covariateRepository.findByProject(projectId)).thenReturn(projectCovariates);

    Map<String, Map<String, Double>> expectedResult = new HashMap<>();
    Map<String, Double> nodeMap = new HashMap<>();
    nodeMap.put(includedCovariate.getName(), includedCovariateStudyValue.getValue());
    expectedResult.put(studyName, nodeMap);
    // execute
    Map<String, Map<String, Double>> result = networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studies);
    assertEquals(expectedResult, result);

    verify(covariateRepository).findByProject(projectId);
  }

  @Test
  public void testBuildCriteriaForInclusionBinom() {
    String dataSourceUuid = "dataSourceUuid";
    when(uuidService.generate()).thenReturn(dataSourceUuid);

    NMAInclusionWithResults inclusionWithResults = buildNmaInclusionMock("binom");
    URI modelUri = URI.create("modelUri");

    // execute
    Map<URI, CriterionEntry> result = networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelUri);

    Map<URI, CriterionEntry> expectedResult = new HashMap<>();
    DataSourceEntry expectedDataSource = new DataSourceEntry(dataSourceUuid, Arrays.asList(0d, 1d), null, "meta analysis", modelUri);
    CriterionEntry expectedEntry = new CriterionEntry(singletonList(expectedDataSource), outcome.getName(), "proportion");
    expectedResult.put(outcome.getSemanticOutcomeUri(), expectedEntry);
    assertEquals(expectedResult, result);

    verify(uuidService).generate();
  }

  @Test
  public void testBuildCriteriaForInclusionNonBinom() {
    String dataSourceUuid = "dataSourceUuid";
    when(uuidService.generate()).thenReturn(dataSourceUuid);

    URI modelUri = URI.create("modelUri");
    NMAInclusionWithResults inclusionWithResults = buildNmaInclusionMock("not binom");

    // execute
    Map<URI, CriterionEntry> result = networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelUri);

    Map<URI, CriterionEntry> expectedResult = new HashMap<>();
    DataSourceEntry expectedDataSource = new DataSourceEntry(dataSourceUuid, "meta analysis", modelUri);
    CriterionEntry expectedEntry = new CriterionEntry(singletonList(expectedDataSource), outcome.getName());
    expectedResult.put(outcome.getSemanticOutcomeUri(), expectedEntry);
    assertEquals(expectedResult, result);

    verify(uuidService).generate();
  }

  private NMAInclusionWithResults buildNmaInclusionMock(String isBinomString) {
    NMAInclusionWithResults inclusionWithResults = mock(NMAInclusionWithResults.class);
    when(inclusionWithResults.getOutcome()).thenReturn(outcome);
    Model model = mock(Model.class);
    when(model.getLikelihood()).thenReturn(isBinomString);
    when(inclusionWithResults.getModel()).thenReturn(model);
    return inclusionWithResults;
  }

  @Test
  public void testBuildAlternativesForInclusion() {
    NMAInclusionWithResults inclusionWithResults = mock(NMAInclusionWithResults.class);
    when(inclusionWithResults.getInterventions()).thenReturn(singleton(fluoxIntervention));

    Map<String, AlternativeEntry> result = networkMetaAnalysisService.buildAlternativesForInclusion(inclusionWithResults);

    Map<String, AlternativeEntry> expectedResult = new HashMap<>();
    AlternativeEntry expectedEntry = new AlternativeEntry(fluoxInterventionId, fluoxIntervention.getName());
    expectedResult.put(fluoxInterventionId.toString(), expectedEntry);

    assertEquals(expectedResult, result);
  }

  @Test
  public void testGetPataviResultsByModelId() throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException {
    URI taskUri = URI.create("taskUri");
    Integer modelId = 321;

    Model modelWithoutTask = mock(Model.class);
    Model modelWithTask= mock(Model.class);
    when(modelWithTask.getTaskUrl()).thenReturn(taskUri);
    when(modelWithTask.getId()).thenReturn(modelId);
    Collection<Model> models = Arrays.asList(modelWithTask, modelWithoutTask);

    List<URI> taskUris = singletonList(taskUri);
    PataviTask task = mock(PataviTask.class);
    List<PataviTask> tasks = singletonList(task);
    when(pataviTaskRepository.findByUrls(taskUris)).thenReturn(tasks);
    Map<URI, JsonNode> results = new HashMap<>();
    JsonNode taskResult = mock(JsonNode.class);
    results.put(taskUri, taskResult);
    when(pataviTaskRepository.getResults(tasks)).thenReturn(results);

    // execute
    Map<Integer, JsonNode> result = networkMetaAnalysisService.getPataviResultsByModelId(models);

    Map<Integer, JsonNode> expectedResult = new HashMap<>();
    expectedResult.put(modelId, taskResult);

    assertEquals(expectedResult, result);

    verify(pataviTaskRepository).findByUrls(taskUris);
    verify(pataviTaskRepository).getResults(tasks);
  }
}