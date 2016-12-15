package org.drugis.addis.problems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.problems.service.model.ContinuousMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  CovariateRepository covariateRepository;

  @Mock
  PerformanceTableBuilder performanceTablebuilder;

  @Mock
  InterventionRepository interventionRepository;

  @Mock
  InterventionService interventionService;

  @Mock
  ModelService modelService;

  @Mock
  OutcomeRepository outcomeRepository;

  @Mock
  PataviTaskRepository pataviTaskRepository;

  @Mock
  MappingService mappingService;

  @Mock
  TrialverseService trialverseService;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  AnalysisService analysisService;

  @InjectMocks
  ProblemService problemService;

  private final String namespaceUid = "UID 1";
  private final String versionedUuid = "versionedUuid";
  private final Integer projectId = 101;
  private final Integer analysisId = 202;
  private final String projectDatasetUid = "projectDatasetUid";
  private final URI projectDatasetVersion = URI.create("http://versions.com/version");

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
  private final Set<AbstractIntervention> allProjectInterventions = Sets.newHashSet(fluoxIntervention, paroxIntervention, sertraIntervention);

  public ProblemServiceTest() throws Exception {
  }

  @Before
  public void setUp() throws URISyntaxException, ResourceDoesNotExistException {
    problemService = new ProblemServiceImpl();
    MockitoAnnotations.initMocks(this);
    when(mappingService.getVersionedUuid(namespaceUid)).thenReturn(versionedUuid);
    when(projectRepository.get(projectId)).thenReturn(project);
    when(interventionRepository.query(project.getId())).thenReturn(allProjectInterventions);
    when(interventionService.resolveCombinations(anyList())).thenReturn(Collections.emptyList());
  }

  @After
  public void cleanUp() throws URISyntaxException {
    verifyNoMoreInteractions(analysisRepository, projectRepository, singleStudyBenefitRiskAnalysisRepository,
            interventionRepository, trialverseService, triplestoreService, mappingService, modelService);
  }

  @Test
  public void testGetSingleStudyBenefitRiskProblem() throws Exception, ReadValueException, InvalidTypeForDoseCheckException {
    URI secondOutcomeUri = URI.create("http://secondSemantic");
    SemanticVariable secondSemanticOutcome = new SemanticVariable(secondOutcomeUri, "second semantic outcome");
    Outcome secondOutcome = new Outcome(-303, projectId, "second outcome", direction, "very", secondSemanticOutcome);
    List<Outcome> outcomes = Arrays.asList(outcome, secondOutcome);
    //include interventions: fluox and sertra
    InterventionInclusion fluoxInclusion = new InterventionInclusion(analysisId, fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(analysisId, sertraIntervention.getId());
    List<InterventionInclusion> interventionInclusions = Arrays.asList(fluoxInclusion, sertraInclusion);
    SingleStudyBenefitRiskAnalysis singleStudyAnalysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "single study analysis", outcomes, interventionInclusions);
    when(analysisRepository.get(analysisId)).thenReturn(singleStudyAnalysis);

    URI defaultMeasurementMoment = URI.create("defaultMeasurementMoment");
    URI daanEtAlUri = URI.create("DaanEtAlUri");
    URI daanEtAlFluoxInstance = URI.create("daanEtAlFluoxInstance");
    URI daanEtAlFluoxArmUri = URI.create("daanEtAlFluoxArm");
    int daanEtAlFluoxSampleSize = 20;
    int daanEtAlFluoxRate = 30;
    URI variableUri = outcome.getSemanticOutcomeUri();
    URI variableConceptUri = outcome.getSemanticOutcomeUri();
    Measurement daanEtAlFluoxMeasurement1 = new Measurement(daanEtAlUri, variableUri, variableConceptUri, daanEtAlFluoxArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlFluoxSampleSize, daanEtAlFluoxRate, null, null, null);
    Measurement daanEtAlFluoxMeasurement2 = new Measurement(daanEtAlUri, secondOutcomeUri, variableConceptUri, daanEtAlFluoxArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlFluoxSampleSize, daanEtAlFluoxRate, null, null, null);
    AbstractSemanticIntervention simpleSemanticFluoxIntervention = new SimpleSemanticIntervention(daanEtAlFluoxInstance, fluoxConceptUri);

    TrialDataArm daanEtAlFluoxArm = new TrialDataArm(daanEtAlFluoxArmUri, "daanEtAlFluoxArm");
    daanEtAlFluoxArm.addMeasurement(defaultMeasurementMoment, daanEtAlFluoxMeasurement1);
    daanEtAlFluoxArm.addMeasurement(defaultMeasurementMoment, daanEtAlFluoxMeasurement2);
    daanEtAlFluoxArm.addSemanticIntervention(simpleSemanticFluoxIntervention);

    URI daanEtAlSertraInstance = URI.create("daanEtAlSertraInstance");
    URI daanEtAlSertraArmUri = URI.create("daanEtAlSertraArm");
    int daanEtAlSertraSampleSize = 40;
    int daanEtAlSertraRate = 5;
    Measurement daanEtAlSertraMeasurement1 = new Measurement(daanEtAlUri, variableUri, variableConceptUri, daanEtAlSertraArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlSertraSampleSize, daanEtAlSertraRate, null, null, null);
    Measurement daanEtAlSertraMeasurement2 = new Measurement(daanEtAlUri, secondOutcomeUri, variableConceptUri, daanEtAlSertraArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlSertraSampleSize, daanEtAlSertraRate, null, null, null);
    AbstractSemanticIntervention simpleSemanticSertraIntervention = new SimpleSemanticIntervention(daanEtAlSertraInstance, sertraConceptUri);

    TrialDataArm daanEtAlSertraArm = new TrialDataArm(daanEtAlSertraArmUri, "daanEtAlSertraArm");
    daanEtAlSertraArm.addMeasurement(defaultMeasurementMoment, daanEtAlSertraMeasurement1);
    daanEtAlSertraArm.addMeasurement(defaultMeasurementMoment, daanEtAlSertraMeasurement2);
    daanEtAlSertraArm.addSemanticIntervention(simpleSemanticSertraIntervention);

    URI daanEtAlParoxInstance = URI.create("daanEtAlParoxInstance");
    URI daanEtAlParoxArmUri = URI.create("daanEtAlParoxArm");
    int daanEtAlParoxSampleSize = 40;
    int daanEtAlParoxRate = 5;
    Measurement daanEtAlParoxMeasurement1 = new Measurement(daanEtAlUri, variableUri, variableConceptUri, daanEtAlParoxArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlParoxSampleSize, daanEtAlParoxRate, null, null, null);
    Measurement daanEtAlParoxMeasurement2 = new Measurement(daanEtAlUri, secondOutcomeUri, variableConceptUri, daanEtAlParoxArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlParoxSampleSize, daanEtAlParoxRate, null, null, null);
    AbstractSemanticIntervention simpleSemanticParoxIntervention = new SimpleSemanticIntervention(daanEtAlParoxInstance, paroxConceptUri);

    TrialDataArm unmatchedDaanEtAlParoxArm = new TrialDataArm(daanEtAlParoxArmUri, "daanEtAlParoxArm");
    unmatchedDaanEtAlParoxArm.addMeasurement(defaultMeasurementMoment, daanEtAlParoxMeasurement1);
    unmatchedDaanEtAlParoxArm.addMeasurement(defaultMeasurementMoment, daanEtAlParoxMeasurement2);
    unmatchedDaanEtAlParoxArm.addSemanticIntervention(simpleSemanticParoxIntervention);

    // add matching result to arms
    daanEtAlFluoxArm.setMatchedProjectInterventionIds(Collections.singleton(fluoxIntervention.getId()));
    daanEtAlSertraArm.setMatchedProjectInterventionIds(Collections.singleton(sertraIntervention.getId()));
    List<TrialDataArm> daanEtAlArms = Arrays.asList(daanEtAlFluoxArm, daanEtAlSertraArm, unmatchedDaanEtAlParoxArm);
    TrialDataStudy daanEtAl = new TrialDataStudy(daanEtAlUri, "Daan et al", daanEtAlArms);
    daanEtAl.setDefaultMeasurementMoment(defaultMeasurementMoment);

    // actually set study in analysis
    singleStudyAnalysis.setStudyGraphUri(daanEtAlUri);

    List<TrialDataStudy> studyResult = Collections.singletonList(daanEtAl);
    Set<URI> outcomeUris = new HashSet<>(Arrays.asList(outcome.getSemanticOutcomeUri(), secondOutcome.getSemanticOutcomeUri()));
    Set<URI> interventionUris = new HashSet<>(Arrays.asList(fluoxIntervention.getSemanticInterventionUri(), sertraIntervention.getSemanticInterventionUri()));

    when(triplestoreService.getSingleStudyData(versionedUuid, daanEtAl.getStudyUri(), project.getDatasetVersion(), outcomeUris, interventionUris)).thenReturn(studyResult);

    AbstractMeasurementEntry measurementEntry = mock(ContinuousMeasurementEntry.class);
    List<AbstractMeasurementEntry> performanceTable = Collections.singletonList(measurementEntry);

    Set<AbstractIntervention> includedInterventions = Sets.newHashSet(fluoxIntervention, sertraIntervention);
    Set<SingleIntervention> singleInterventions = Sets.newHashSet(fluoxIntervention, sertraIntervention);
    when(analysisService.getIncludedInterventions(singleStudyAnalysis)).thenReturn(includedInterventions);
    when(analysisService.getSingleInterventions(allProjectInterventions)).thenReturn(singleInterventions);
    when(analysisService.getSingleInterventions(includedInterventions)).thenReturn(singleInterventions);
    when(triplestoreService.findMatchingIncludedInterventions(includedInterventions, daanEtAlFluoxArm)).thenReturn(ImmutableSet.of(fluoxIntervention));
    when(triplestoreService.findMatchingIncludedInterventions(includedInterventions, daanEtAlSertraArm)).thenReturn(ImmutableSet.of(sertraIntervention));
    when(triplestoreService.findMatchingIncludedInterventions(includedInterventions, unmatchedDaanEtAlParoxArm)).thenReturn(Collections.emptySet());

    when(performanceTablebuilder.build(any())).thenReturn(performanceTable);
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(versionedUuid);

    // --------------- execute ---------------- //
    SingleStudyBenefitRiskProblem actualProblem = (SingleStudyBenefitRiskProblem) problemService.getProblem(projectId, analysisId);
    // --------------- execute ---------------- //

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(triplestoreService).getSingleStudyData(versionedUuid, daanEtAl.getStudyUri(), project.getDatasetVersion(), outcomeUris, interventionUris);
    verify(triplestoreService).findMatchingIncludedInterventions(includedInterventions, daanEtAlFluoxArm);
    verify(triplestoreService).findMatchingIncludedInterventions(includedInterventions, daanEtAlSertraArm);
    verify(triplestoreService).findMatchingIncludedInterventions(includedInterventions, unmatchedDaanEtAlParoxArm);
    verify(performanceTablebuilder).build(any());
    verify(mappingService).getVersionedUuid(project.getNamespaceUid());
    verify(interventionRepository).query(project.getId());

    Pair<Measurement, Integer> pair1 = Pair.of(daanEtAlFluoxMeasurement1, daanEtAlFluoxArm.getMatchedProjectInterventionIds().iterator().next());
    Pair<Measurement, Integer> pair2 = Pair.of(daanEtAlFluoxMeasurement2, daanEtAlFluoxArm.getMatchedProjectInterventionIds().iterator().next());
    Pair<Measurement, Integer> pair3 = Pair.of(daanEtAlSertraMeasurement1, daanEtAlSertraArm.getMatchedProjectInterventionIds().iterator().next());
    Pair<Measurement, Integer> pair4 = Pair.of(daanEtAlSertraMeasurement2, daanEtAlSertraArm.getMatchedProjectInterventionIds().iterator().next());
    Set<Pair<Measurement, Integer>> instancePairs = ImmutableSet.of(pair1, pair2, pair3, pair4);
    verify(performanceTablebuilder).build(instancePairs);

    assertNotNull(actualProblem);
    assertNotNull(actualProblem.getTitle());
    assertEquals(singleStudyAnalysis.getTitle(), actualProblem.getTitle());
    assertNotNull(actualProblem.getAlternatives());
    assertNotNull(actualProblem.getCriteria());

    Map<URI, CriterionEntry> actualCriteria = actualProblem.getCriteria();
    assertTrue(actualCriteria.keySet().contains(variableUri));
    assertTrue(actualCriteria.keySet().contains(secondOutcomeUri));
  }

  @Test
  public void testGetNmaProblemDichotomous() throws URISyntaxException, SQLException, IOException, ReadValueException, ResourceDoesNotExistException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {

    // analysis
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);
    when(analysisRepository.get(networkMetaAnalysis.getId())).thenReturn(networkMetaAnalysis);

    //include interventions: fluox and sertra
    InterventionInclusion fluoxInclusion = new InterventionInclusion(networkMetaAnalysis.getId(), fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(networkMetaAnalysis.getId(), sertraIntervention.getId());
    HashSet<InterventionInclusion> interventionInclusions = new HashSet<>(Arrays.asList(fluoxInclusion, sertraInclusion));
    networkMetaAnalysis.updateIncludedInterventions(interventionInclusions);

    // covariates
    Integer includedCovariateId = -1;
    Integer excludedCovariateId = -2;
    String includedCovariateDefinitionKey = CovariateOption.ALLOCATION_RANDOMIZED.toString();
    Covariate includedCovariate = new Covariate(includedCovariateId, project.getId(), "isRandomised", "mot",
            includedCovariateDefinitionKey, CovariateOptionType.STUDY_CHARACTERISTIC);
    Covariate notIncludedCovariate = new Covariate(excludedCovariateId, project.getId(), "age", "mot", "ageUri", CovariateOptionType.POPULATION_CHARACTERISTIC);
    Collection<Covariate> allProjectCovariates = Arrays.asList(includedCovariate, notIncludedCovariate);
    when(covariateRepository.findByProject(project.getId())).thenReturn(allProjectCovariates);

    Set<CovariateInclusion> covariateInclusions = new HashSet<>(Collections.singletonList(new CovariateInclusion(networkMetaAnalysis.getId(), includedCovariateId)));
    networkMetaAnalysis.updateIncludedCovariates(covariateInclusions);

    // add study with excluded arms to check whether it's excluded from covariate entries
    URI nonDefaultMMUri = URI.create("nonDefaultMeasurementMomentUri");
    TrialDataStudy daanEtAl = createStudyMock(networkMetaAnalysis, includedCovariate, URI.create("DaanEtAlUri"), "DaanEtal");
    TrialDataStudy nonDefaultMM = createStudyMock(networkMetaAnalysis, includedCovariate, URI.create("nonDefaultMMUri"), "nonDefaultMM", nonDefaultMMUri);
    TrialDataStudy pietEtAl = createStudyMock(networkMetaAnalysis, includedCovariate, URI.create("PietEtAlUri"), "PietEtal");
    List<ArmExclusion> excludedArms = new ArrayList<>(networkMetaAnalysis.getExcludedArms());
    excludedArms.addAll(pietEtAl.getTrialDataArms().stream()
            .map(arm -> new ArmExclusion(networkMetaAnalysis.getId(), arm.getUri()))
            .collect(Collectors.toList()));
    networkMetaAnalysis.updateArmExclusions(new HashSet<>(excludedArms));
    List<TrialDataStudy> trialDataStudies = Arrays.asList(daanEtAl, pietEtAl, nonDefaultMM);
    networkMetaAnalysis.updateIncludedMeasurementMoments(Sets.newHashSet(new MeasurementMomentInclusion(analysisId, nonDefaultMM.getStudyUri(), nonDefaultMMUri)));
    when(analysisService.buildEvidenceTable(project.getId(), networkMetaAnalysis.getId())).thenReturn(trialDataStudies);


    // --------------- execute ---------------- //
    final AbstractProblem problem = problemService.getProblem(project.getId(), networkMetaAnalysis.getId());
    // --------------- execute ---------------- //


    assertNotNull(problem);
    assertTrue(problem instanceof NetworkMetaAnalysisProblem);
    NetworkMetaAnalysisProblem networkProblem = (NetworkMetaAnalysisProblem) problem;
    assertEquals(interventionInclusions.size(), networkProblem.getTreatments().size());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = networkProblem.getEntries();
    assertNotNull(entries);
    assertEquals(4, entries.size());
    RateNetworkMetaAnalysisProblemEntry fluoxEntry = (RateNetworkMetaAnalysisProblemEntry) entries.get(0);
    assertEquals(fluoxIntervention.getId(), fluoxEntry.getTreatment());
    RateNetworkMetaAnalysisProblemEntry sertraEntry = (RateNetworkMetaAnalysisProblemEntry) entries.get(1);
    assertEquals(sertraIntervention.getId(), sertraEntry.getTreatment());

    TreatmentEntry fluoxTreatmentEntry = new TreatmentEntry(fluoxIntervention.getId(), fluoxIntervention.getName());
    TreatmentEntry sertraTreatmentEntry = new TreatmentEntry(sertraIntervention.getId(), sertraIntervention.getName());
    Set<TreatmentEntry> expectedTreatments = Sets.newHashSet(fluoxTreatmentEntry, sertraTreatmentEntry);
    assertEquals(expectedTreatments, Sets.newHashSet(networkProblem.getTreatments()));

    Map<String, Map<String, Double>> studyLevelCovariates = networkProblem.getStudyLevelCovariates();
    assertEquals(covariateInclusions.size() * 2 /* 2 studies */, studyLevelCovariates.size());
    assertEquals(daanEtAl.getName(), studyLevelCovariates.keySet().toArray()[0]);
    Map<String, Double> covariateEntry = (Map<String, Double>) studyLevelCovariates.values().toArray()[0];
    assertEquals(includedCovariate.getName(), covariateEntry.keySet().toArray()[0]);

    verify(projectRepository).get(project.getId());
    verify(analysisRepository).get(networkMetaAnalysis.getId());
    verify(interventionRepository).query(project.getId());
    verify(covariateRepository, times(2)).findByProject(project.getId());
  }

  @Test
  public void testGetNmaProblemContinuous() throws URISyntaxException, SQLException, IOException, ReadValueException, ResourceDoesNotExistException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {

    // analysis
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);
    when(analysisRepository.get(networkMetaAnalysis.getId())).thenReturn(networkMetaAnalysis);

    //include interventions: fluox and sertra
    InterventionInclusion fluoxInclusion = new InterventionInclusion(networkMetaAnalysis.getId(), fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(networkMetaAnalysis.getId(), sertraIntervention.getId());
    HashSet<InterventionInclusion> interventionInclusions = new HashSet<>(Arrays.asList(fluoxInclusion, sertraInclusion));
    networkMetaAnalysis.updateIncludedInterventions(interventionInclusions);

    TrialDataStudy daanEtAl = createStudyMock(networkMetaAnalysis, null, URI.create("DaanEtAlUri"), "DaanEtal");
    TrialDataStudy pietEtAl = createStudyMock(networkMetaAnalysis, null, URI.create("PietEtAlUri"), "PietEtAl");

    setContinuousMeasurements(daanEtAl);
    setContinuousMeasurements(pietEtAl);

    List<TrialDataStudy> trialDataStudies = Arrays.asList(daanEtAl, pietEtAl);
    when(analysisService.buildEvidenceTable(project.getId(), networkMetaAnalysis.getId())).thenReturn(trialDataStudies);

    // --------------- execute ---------------- //
    final AbstractProblem problem = problemService.getProblem(project.getId(), networkMetaAnalysis.getId());
    // --------------- execute ---------------- //


    assertNotNull(problem);
    assertTrue(problem instanceof NetworkMetaAnalysisProblem);
    NetworkMetaAnalysisProblem networkProblem = (NetworkMetaAnalysisProblem) problem;
    assertEquals(interventionInclusions.size(), networkProblem.getTreatments().size());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = networkProblem.getEntries();
    assertNotNull(entries);
    assertEquals(4, entries.size());
    ContinuousStdErrEntry fluoxEntry = (ContinuousStdErrEntry) entries.get(0);
    assertEquals(fluoxIntervention.getId(), fluoxEntry.getTreatment());
    ContinuousStdErrEntry sertraEntry = (ContinuousStdErrEntry) entries.get(1);
    assertEquals(sertraIntervention.getId(), sertraEntry.getTreatment());

    TreatmentEntry fluoxTreatmentEntry = new TreatmentEntry(fluoxIntervention.getId(), fluoxIntervention.getName());
    TreatmentEntry sertraTreatmentEntry = new TreatmentEntry(sertraIntervention.getId(), sertraIntervention.getName());
    Set<TreatmentEntry> expectedTreatments = Sets.newHashSet(fluoxTreatmentEntry, sertraTreatmentEntry);
    assertEquals(expectedTreatments, Sets.newHashSet(networkProblem.getTreatments()));

    verify(projectRepository).get(project.getId());
    verify(analysisRepository).get(networkMetaAnalysis.getId());
    verify(interventionRepository).query(project.getId());
    verify(covariateRepository).findByProject(project.getId());
  }

  private void setContinuousMeasurements(TrialDataStudy study) {
    URI defaultMeasurementMomentUri = URI.create("defaultMeasurementMoment");
    String title = study.getName();
    URI uri = study.getStudyUri();
    URI variableUri = outcome.getSemanticOutcomeUri();
    URI variableConceptUri = outcome.getSemanticOutcomeUri();
    URI daanEtAlFluoxArmUri = URI.create(title + "FluoxArm");
    Integer daanEtAlFluoxSampleSize = 9001;
    Double daanEtAlFluoxStdErr = 0.4;
    Measurement defaultDaanEtAlFluoxMeasurement = new Measurement(uri, variableUri, variableConceptUri, daanEtAlFluoxArmUri,
            CONTINUOUS_TYPE_URI, daanEtAlFluoxSampleSize, null, null, daanEtAlFluoxStdErr, null);
    URI daanEtAlSertraArmUri = URI.create(title + "SertraArm");
    Integer daanEtAlSertraSampleSize = 42;
    Double daanEtAlSertraMean = 0.43;
    Double daanEtAlSertraStdDev = 0.33;

    Measurement daanEtAlSertraMeasurement = new Measurement(uri, variableUri, variableConceptUri, daanEtAlSertraArmUri,
            CONTINUOUS_TYPE_URI, daanEtAlSertraSampleSize, null, daanEtAlSertraStdDev, null, daanEtAlSertraMean);

    study.getTrialDataArms().forEach(arm -> arm.getMeasurements().clear());
    TrialDataArm fluoxArm = study.getTrialDataArms().get(0);
    fluoxArm.addMeasurement(defaultMeasurementMomentUri, defaultDaanEtAlFluoxMeasurement);
    TrialDataArm sertraArm = study.getTrialDataArms().get(1);
    sertraArm.addMeasurement(defaultMeasurementMomentUri, daanEtAlSertraMeasurement);
  }

  private TrialDataStudy createStudyMock(NetworkMetaAnalysis networkMetaAnalysis, Covariate includedCovariate, URI uri, String title) {
    return createStudyMock(networkMetaAnalysis, includedCovariate, uri, title, null);
  }

  private TrialDataStudy createStudyMock(NetworkMetaAnalysis networkMetaAnalysis, Covariate includedCovariate, URI uri, String title, URI nonDefaultMeasurementMoment) {
    URI measurementMoment = (nonDefaultMeasurementMoment == null) ? URI.create("defaultMeasurementMoment") : nonDefaultMeasurementMoment;
    URI variableUri = outcome.getSemanticOutcomeUri();
    URI variableConceptUri = outcome.getSemanticOutcomeUri();

    URI daanEtAlFluoxInstance = URI.create(title + "FluoxInstance");
    URI daanEtAlFluoxArmUri = URI.create(title + "FluoxArm");
    int daanEtAlFluoxSampleSize = 20;
    int daanEtAlFluoxRate = 30;
    Measurement defaultDaanEtAlFluoxMeasurement = new Measurement(uri, variableUri, variableConceptUri, daanEtAlFluoxArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlFluoxSampleSize, daanEtAlFluoxRate, null, null, null);
    AbstractSemanticIntervention simpleSemanticFluoxIntervention = new SimpleSemanticIntervention(daanEtAlFluoxInstance, fluoxConceptUri);

    TrialDataArm daanEtAlFluoxArm = new TrialDataArm(daanEtAlFluoxArmUri, "daanEtAlFluoxArm");
    daanEtAlFluoxArm.addMeasurement(measurementMoment, defaultDaanEtAlFluoxMeasurement);
    daanEtAlFluoxArm.addSemanticIntervention(simpleSemanticFluoxIntervention);

    URI daanEtAlSertraInstance = URI.create(title + "SertraInstance");
    URI daanEtAlSertraArmUri = URI.create(title + "SertraArm");
    int daanEtAlSertraSampleSize = 40;
    int daanEtAlSertraRate = 5;
    Measurement daanEtAlSertraMeasurement = new Measurement(uri, variableUri, variableConceptUri, daanEtAlSertraArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlSertraSampleSize, daanEtAlSertraRate, null, null, null);
    AbstractSemanticIntervention simpleSemanticSertraIntervention = new SimpleSemanticIntervention(daanEtAlSertraInstance, sertraConceptUri);

    TrialDataArm daanEtAlSertraArm = new TrialDataArm(daanEtAlSertraArmUri, title + "SertraArm");
    daanEtAlSertraArm.addMeasurement(measurementMoment, daanEtAlSertraMeasurement);
    daanEtAlFluoxArm.addSemanticIntervention(simpleSemanticSertraIntervention);

    URI daanEtAlExcludedArmUri = URI.create(title + "excludeme");
    Measurement daanEtAlExcludedMeasurement = new Measurement(uri, variableUri, variableConceptUri, daanEtAlExcludedArmUri,
            DICHOTOMOUS_TYPE_URI, daanEtAlSertraSampleSize, daanEtAlSertraRate, null, null, null);
    TrialDataArm excludedArm = new TrialDataArm(daanEtAlSertraArmUri, title + "excludedArm");
    excludedArm.addMeasurement(measurementMoment, daanEtAlExcludedMeasurement);
    excludedArm.addSemanticIntervention(simpleSemanticSertraIntervention);

    // exclude arms
    Set<ArmExclusion> excludedArms = new HashSet<>(Collections.singletonList(new ArmExclusion(networkMetaAnalysis.getId(), daanEtAlExcludedArmUri)));
    networkMetaAnalysis.updateArmExclusions(excludedArms);

    // add matching result to arms
    daanEtAlFluoxArm.setMatchedProjectInterventionIds(new HashSet<>(Collections.singletonList(fluoxIntervention.getId())));
    daanEtAlSertraArm.setMatchedProjectInterventionIds(new HashSet<>(Collections.singletonList(sertraIntervention.getId())));
    List<TrialDataArm> daanEtAlArms = Arrays.asList(daanEtAlFluoxArm, daanEtAlSertraArm, excludedArm);
    TrialDataStudy daanEtAl = new TrialDataStudy(uri, title, daanEtAlArms);
    Double randomisedValue = 30d;
    if(includedCovariate != null) {
      daanEtAl.addCovariateValue(new CovariateStudyValue(uri, includedCovariate.getDefinitionKey(), randomisedValue));
    }
    daanEtAl.setDefaultMeasurementMoment(measurementMoment);
    return daanEtAl;
  }

  @Test
  public void testGetMetaBRProblem() throws Exception, ReadValueException, InvalidTypeForDoseCheckException {

    URI version = URI.create("http://versions.com/version");
    Integer projectId = 1;
    Integer analysisId = 2;
    String title = "title";

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    when(project.getNamespaceUid()).thenReturn(namespaceUid);
    when(project.getDatasetVersion()).thenReturn(version);

    Set<InterventionInclusion> includedAlternatives = new HashSet<>(3);
    SingleIntervention intervention1 = new SimpleIntervention(11, projectId, "fluox", "", new SemanticInterventionUriAndName(URI.create("uri1"), "fluoxS"));
    SingleIntervention intervention2 = new SimpleIntervention(12, projectId, "parox", "", new SemanticInterventionUriAndName(URI.create("uri2"), "paroxS"));
    SingleIntervention intervention3 = new SimpleIntervention(13, projectId, "sertr", "", new SemanticInterventionUriAndName(URI.create("uri3"), "sertrS"));
    includedAlternatives.addAll(Arrays.asList(new InterventionInclusion(analysisId, 11), new InterventionInclusion(analysisId, 12), new InterventionInclusion(analysisId, 13)));

    SingleIntervention intervention4 = new SimpleIntervention(14, projectId, "foo", "", new SemanticInterventionUriAndName(URI.create("uri4"), "fooS"));
    Set<AbstractIntervention> interventions = Sets.newHashSet(intervention1, intervention2, intervention3, intervention4);

    PataviTask pataviTask1 = new PataviTask(TestUtils.buildPataviTaskJson("taskId1"));
    PataviTask pataviTask2 = new PataviTask(TestUtils.buildPataviTaskJson("taskId2"));
    List<PataviTask> pataviTasks = Arrays.asList(pataviTask1, pataviTask2);

    Model model1 = new Model.ModelBuilder(analysisId, "model 1")
            .id(71)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .taskUri(pataviTask1.getSelf())
            .likelihood(Model.LIKELIHOOD_BINOM)
            .link(Model.LINK_IDENTITY)
            .build();
    Model model2 = new Model.ModelBuilder(analysisId, "model 2")
            .id(72)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .taskUri(pataviTask2.getSelf())
            .likelihood(Model.LIKELIHOOD_BINOM)
            .link(Model.LINK_CLOGLOG)
            .build();
    List<Model> models = Arrays.asList(model1, model2);

    Outcome outcome1 = new Outcome(21, projectId, "ham", direction, "", new SemanticVariable(URI.create("outUri1"), "hamS"));
    Outcome outcome2 = new Outcome(22, projectId, "headache", direction, "", new SemanticVariable(URI.create("outUri2"), "headacheS"));
    List<Outcome> outcomes = Arrays.asList(outcome1, outcome2);


    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, title, includedAlternatives);
    Integer nma1Id = 31;
    Integer nma2Id = 32;
    String baseline1JsonString = "{\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";
    String baseline2JsonString = "{\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";

    MbrOutcomeInclusion inclusion1 = new MbrOutcomeInclusion(analysisId, outcome1.getId(), nma1Id, model1.getId());
    inclusion1.setBaseline(baseline1JsonString);
    MbrOutcomeInclusion inclusion2 = new MbrOutcomeInclusion(analysisId, outcome2.getId(), nma2Id, model2.getId());
    inclusion2.setBaseline(baseline2JsonString);
    List<MbrOutcomeInclusion> outcomeInclusions = Arrays.asList(inclusion1, inclusion2);
    analysis.setMbrOutcomeInclusions(outcomeInclusions);

    ObjectMapper om = new ObjectMapper();
    String results1 = "{\n" +
            "  \"multivariateSummary\": {\n" +
            "    \"11\": {\n" +
            "      \"mu\": {\n" +
            "        \"d.11.12\": 0.55302,\n" +
            "        \"d.11.13\": 0.46622\n" +
            "      },\n" +
            "      \"sigma\": {\n" +
            "        \"d.11.12\": {\n" +
            "          \"d.11.12\": 74.346,\n" +
            "          \"d.11.13\": 1.9648\n" +
            "        },\n" +
            "        \"d.11.13\": {\n" +
            "          \"d.11.12\": 1.9648,\n" +
            "          \"d.11.13\": 74.837\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    Map<URI, JsonNode> results = new HashMap<>();
    JsonNode task1Results = om.readTree(results1);
    JsonNode task2Results = om.readTree(results1);
    results.put(pataviTask1.getSelf(), task1Results);
    results.put(pataviTask2.getSelf(), task2Results);

    List<Integer> modelIds = Arrays.asList(model2.getId(), model1.getId());
    List<Integer> outcomeIds = Arrays.asList(outcome2.getId(), outcome1.getId());
    when(projectRepository.get(projectId)).thenReturn(project);
    when(modelService.get(modelIds)).thenReturn(models);
    when(outcomeRepository.get(projectId, outcomeIds)).thenReturn(outcomes);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    List<URI> taskIds = Arrays.asList(model1.getTaskUrl(), model2.getTaskUrl());
    when(pataviTaskRepository.findByUrls(taskIds)).thenReturn(pataviTasks);
    when(pataviTaskRepository.getResults(taskIds)).thenReturn(results);
    when(interventionRepository.query(projectId)).thenReturn(interventions);

    MetaBenefitRiskProblem problem = (MetaBenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(modelService).get(modelIds);
    verify(outcomeRepository).get(projectId, outcomeIds);
    verify(analysisRepository).get(analysisId);
    verify(pataviTaskRepository).findByUrls(taskIds);
    verify(pataviTaskRepository).getResults(taskIds);
    verify(interventionRepository).query(projectId);

    assertEquals(3, problem.getAlternatives().size());
    assertEquals(2, problem.getCriteria().size());
    assertEquals(2, problem.getPerformanceTable().size());
    assertEquals("relative-cloglog-normal", problem.getPerformanceTable().get(0).getPerformance().getType());
    assertEquals("relative-normal", problem.getPerformanceTable().get(1).getPerformance().getType());
    List<List<Double>> expectedDataHeadache = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
    assertEquals(expectedDataHeadache, problem.getPerformanceTable().get(0).getPerformance().getParameters().getRelative().getCov().getData());
    List<List<Double>> expectedDataHam = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
    assertEquals(expectedDataHam, problem.getPerformanceTable().get(1).getPerformance().getParameters().getRelative().getCov().getData());
  }

  @Test
  public void applyModelSettingsSensitivity() throws InvalidModelException {
    String studyToOmit = "studyToOmit";
    String studyToLeace = "studyToLeave";
    AbstractNetworkMetaAnalysisProblemEntry toOmit1 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 1, 1, 1);
    AbstractNetworkMetaAnalysisProblemEntry toOmit2 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 2, 2, 2);
    AbstractNetworkMetaAnalysisProblemEntry toLeave = new RateNetworkMetaAnalysisProblemEntry(studyToLeace, 3, 3, 3);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(toOmit1, toOmit2, toLeave);
    List<TreatmentEntry> treatments = Collections.emptyList();
    Map<String, Map<String, Double>> covariates = Collections.emptyMap();
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments, covariates);
    JSONObject sensitivity = new JSONObject();
    sensitivity.put("omittedStudy", studyToOmit);
    Model model = new Model.ModelBuilder(1, "model")
            .link(Model.LINK_IDENTITY)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .sensitivity(sensitivity)
            .build();
    NetworkMetaAnalysisProblem result = problemService.applyModelSettings(problem, model);
    assertEquals(1, result.getEntries().size());
    assertFalse(result.getEntries().contains(toOmit1));
    assertFalse(result.getEntries().contains(toOmit2));
    assertTrue(result.getEntries().contains(toLeave));
  }

  @Test
  public void applyModelSettingsNoSensitivity() throws InvalidModelException {
    String studyToOmit = "studyToOmit";
    AbstractNetworkMetaAnalysisProblemEntry entry1 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 1, 1, 1);
    AbstractNetworkMetaAnalysisProblemEntry entry2 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 2, 2, 2);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(entry1, entry2);
    List<TreatmentEntry> treatments = Collections.emptyList();
    Map<String, Map<String, Double>> covariates = Collections.emptyMap();
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments, covariates);
    JSONObject sensitivity = new JSONObject();
    sensitivity.put("omittedStudy", studyToOmit);
    Model model = new Model.ModelBuilder(1, "model")
            .link(Model.LINK_IDENTITY)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .sensitivity(sensitivity)
            .build();
    NetworkMetaAnalysisProblem result = problemService.applyModelSettings(problem, model);
    assertEquals(Collections.emptyList(), result.getEntries());
  }

}
