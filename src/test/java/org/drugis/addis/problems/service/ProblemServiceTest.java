package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import net.minidev.json.JSONObject;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.NetworkPerformanceTableBuilder;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.util.service.UuidService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Created by daan on 3/21/14.
 */
public class ProblemServiceTest {

  @Mock
  private AnalysisRepository analysisRepository;

  @Mock
  private AnalysisService analysisService;

  @Mock
  private LinkService linkService;

  @Mock
  private ModelService modelService;

  @Mock
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Mock
  private NetworkPerformanceTableBuilder networkPerformanceTableBuilder;

  @Mock
  private OutcomeRepository outcomeRepository;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private SingleStudyBenefitRiskService singleStudyBenefitRiskService;

  @Mock
  private UuidService uuidService;

  @InjectMocks
  private ProblemService problemService;

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
          "fluoxetine", "motivation", fluoxConcept.getUri(), fluoxConcept.getLabel());
  private final URI paroxConceptUri = URI.create("paroxConceptUri");
  private final SemanticInterventionUriAndName paroxConcept = new SemanticInterventionUriAndName(paroxConceptUri, "parox concept");
  private final Integer paroxInterventionId = 402;
  private final SingleIntervention paroxIntervention = new SimpleIntervention(paroxInterventionId, project.getId(),
          "paroxetine", "motivation", paroxConcept.getUri(), paroxConcept.getLabel());
  private final URI sertraConceptUri = URI.create("sertraConceptUri");
  private final SemanticInterventionUriAndName sertraConcept = new SemanticInterventionUriAndName(sertraConceptUri, "sertra concept");
  private final Integer sertraInterventionId = 403;
  private final SingleIntervention sertraIntervention = new SimpleIntervention(sertraInterventionId, project.getId(),
          "sertraline", "motivation", sertraConcept.getUri(), sertraConcept.getLabel());
  private final Set<AbstractIntervention> allProjectInterventions = Sets.newHashSet(fluoxIntervention, paroxIntervention, sertraIntervention);

  // empty constructor so exception from field initialisation can go somewhere
  public ProblemServiceTest() throws Exception {
  }

  @Before
  public void setUp() throws ResourceDoesNotExistException {
    problemService = new ProblemServiceImpl();
    MockitoAnnotations.initMocks(this);
    when(projectRepository.get(projectId)).thenReturn(project);
    }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(analysisRepository, projectRepository,
            modelService, singleStudyBenefitRiskService, uuidService, linkService,
            networkMetaAnalysisService
      );
  }

  @Test
  public void testGetProblemSingleStudyBenefitRisk() throws Exception, ProblemCreationException {
    // prepare outcomes
    URI secondOutcomeUri = URI.create("http://secondSemantic");
    SemanticVariable secondSemanticOutcome = new SemanticVariable(secondOutcomeUri, "second semantic outcome");
    Outcome secondOutcome = new Outcome(-303, projectId, "second outcome", direction, "very", secondSemanticOutcome);
    List<Outcome> outcomes = Arrays.asList(outcome, secondOutcome);
    when(outcomeRepository.get(projectId, Sets.newHashSet(outcome.getId(), secondOutcome.getId()))).thenReturn(outcomes);


    URI defaultMeasurementMoment = URI.create("defaultMeasurementMoment");
    TrialDataStudy studyMock = mock(TrialDataStudy.class);
    when(studyMock.getDefaultMeasurementMoment()).thenReturn(defaultMeasurementMoment);
    URI daanEtAlUri = URI.create("daanEtAl");
    SingleStudyContext context = mock(SingleStudyContext.class);

    //include interventions: fluox and sertra
    InterventionInclusion fluoxInclusion = new InterventionInclusion(analysisId, fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(analysisId, sertraIntervention.getId());
    Set<InterventionInclusion> interventionInclusions = Sets.newHashSet(fluoxInclusion, sertraInclusion);

    // build analysis & return it
    BenefitRiskAnalysis singleStudyAnalysis = new BenefitRiskAnalysis(analysisId, projectId, "single study analysis", interventionInclusions);
    BenefitRiskStudyOutcomeInclusion outcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, outcome.getId(), daanEtAlUri);
    BenefitRiskStudyOutcomeInclusion secondOutcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, secondOutcome.getId(), daanEtAlUri);
    singleStudyAnalysis.setBenefitRiskStudyOutcomeInclusions(Arrays.asList(outcomeInclusion, secondOutcomeInclusion));
    when(analysisRepository.get(analysisId)).thenReturn(singleStudyAnalysis);

    // prepare interventions for retrieval
    Set<AbstractIntervention> includedInterventions = Sets.newHashSet(fluoxIntervention, sertraIntervention);
    Set<SingleIntervention> singleInterventions = Sets.newHashSet(fluoxIntervention, sertraIntervention);
    when(analysisService.getIncludedInterventions(singleStudyAnalysis)).thenReturn(includedInterventions);
    when(analysisService.getSingleInterventions(allProjectInterventions)).thenReturn(singleInterventions);
    when(analysisService.getSingleInterventions(includedInterventions)).thenReturn(singleInterventions);

    // prepare single study BR service
    when(singleStudyBenefitRiskService.buildContext(project, daanEtAlUri, Sets.newHashSet(outcomes), includedInterventions)).thenReturn(context);
    when(singleStudyBenefitRiskService.getSingleStudyMeasurements(project, daanEtAlUri, context)).thenReturn(studyMock);
    List<TrialDataArm> armsMock = Arrays.asList(mock(TrialDataArm.class), mock(TrialDataArm.class));
    when(singleStudyBenefitRiskService.getArmsWithMatching(includedInterventions, studyMock)).thenReturn(armsMock);
    Map<URI, CriterionEntry> criteriaMock = new HashMap<>();
    criteriaMock.put(URI.create("criterion1"), mock(CriterionEntry.class));
    when(singleStudyBenefitRiskService.getCriteria(armsMock, defaultMeasurementMoment, context)).thenReturn(criteriaMock);
    Map<String, AlternativeEntry> alternativesMock = new HashMap<>();
    alternativesMock.put(String.valueOf(fluoxInterventionId), mock(AlternativeEntry.class));
    alternativesMock.put(String.valueOf(sertraInterventionId), mock(AlternativeEntry.class));
    when(singleStudyBenefitRiskService.getAlternatives(armsMock, context)).thenReturn(alternativesMock);
    Set<MeasurementWithCoordinates> measurementsMock = new HashSet<>();
    when(singleStudyBenefitRiskService.getMeasurementsWithCoordinates(armsMock, defaultMeasurementMoment, context)).thenReturn(measurementsMock);
    List<AbstractMeasurementEntry> performanceMock = singletonList(mock(AbstractMeasurementEntry.class));
    when(singleStudyBenefitRiskService.buildPerformanceTable(measurementsMock)).thenReturn(performanceMock);


    // --------------- execute ---------------- //
    BenefitRiskProblem actualProblem = (BenefitRiskProblem) problemService.getProblem(projectId, analysisId);
    // --------------- execute ---------------- //

    BenefitRiskProblem expectedProblem = new BenefitRiskProblem(WebConstants.SCHEMA_VERSION, criteriaMock, alternativesMock, performanceMock);

    assertEquals(expectedProblem, actualProblem);

    verify(modelService).get(Collections.emptySet());
    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);

    verify(singleStudyBenefitRiskService).buildContext(project, daanEtAlUri, Sets.newHashSet(outcomes), includedInterventions);
    verify(singleStudyBenefitRiskService).getSingleStudyMeasurements(project, daanEtAlUri, context);
    verify(singleStudyBenefitRiskService).getArmsWithMatching(includedInterventions, studyMock);
    verify(singleStudyBenefitRiskService).getCriteria(armsMock, defaultMeasurementMoment, context);
    verify(singleStudyBenefitRiskService).getAlternatives(armsMock, context);
    verify(singleStudyBenefitRiskService).getMeasurementsWithCoordinates(armsMock, defaultMeasurementMoment, context);
    verify(singleStudyBenefitRiskService).buildPerformanceTable(measurementsMock);
  }

  @Test
  public void testGetProblemNMA() throws URISyntaxException, ReadValueException, ResourceDoesNotExistException, ProblemCreationException {

    // analysis
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);

    List<TreatmentEntry> treatments = singletonList(mock(TreatmentEntry.class));
    List<TrialDataStudy> studies = singletonList(mock(TrialDataStudy.class));
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = singletonList(mock(AbstractNetworkMetaAnalysisProblemEntry.class));
    List<TrialDataStudy> studiesWithEntries = singletonList(mock(TrialDataStudy.class));

    Map<String, Map<String, Double>> studyLevelCovariates = new HashMap<>();
    Map<String, Double> covariateValues= new HashMap<>();
    covariateValues.put("covariateName", 1.0);
    studyLevelCovariates.put("studyName", covariateValues);

    when(networkMetaAnalysisService.getTreatments(analysis)).thenReturn(treatments);
    when(analysisService.buildEvidenceTable(project.getId(), analysis.getId())).thenReturn(studies);
    when(networkMetaAnalysisService.buildPerformanceEntries(analysis, studies)).thenReturn(entries);

    // remove studies without entries from final list
    when(networkMetaAnalysisService.getStudiesWithEntries(studies, entries)).thenReturn(studiesWithEntries);

    // add covariate values to problem
    when(networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studiesWithEntries)).thenReturn(studyLevelCovariates);

    // --------------- execute ---------------- //
    final AbstractProblem result = problemService.getProblem(project.getId(), analysis.getId());
    // --------------- execute ---------------- //

    NetworkMetaAnalysisProblem expectedResult = new NetworkMetaAnalysisProblem(entries, treatments, studyLevelCovariates);
    assertEquals(expectedResult, result);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(networkMetaAnalysisService).getTreatments(analysis);
    verify(analysisService).buildEvidenceTable(project.getId(), analysis.getId());
    verify(networkMetaAnalysisService).buildPerformanceEntries(analysis, studies);
    verify(networkMetaAnalysisService).getStudiesWithEntries(studies, entries);
    verify(networkMetaAnalysisService).getStudyLevelCovariates(project, analysis, studiesWithEntries);
  }

  @Test
  public void testGetProblemMetaBR() throws Exception, ProblemCreationException {

    URI version = URI.create("http://versions.com/version");
    Integer projectId = 1;
    Integer analysisId = 2;
    String title = "title";

    Project project = mock(Project.class);
    when(project.getId()).thenReturn(projectId);
    String namespaceUuid = "UUID 1";
    when(project.getNamespaceUid()).thenReturn(namespaceUuid);
    when(project.getDatasetVersion()).thenReturn(version);
    when(project.getOwner()).thenReturn(owner);

    Set<InterventionInclusion> includedAlternatives = new HashSet<>(3);
    includedAlternatives.addAll(Arrays.asList(new InterventionInclusion(analysisId, 11), new InterventionInclusion(analysisId, 12), new InterventionInclusion(analysisId, 13)));


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

    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, title, includedAlternatives);
    Integer nma1Id = 31;
    Integer nma2Id = 32;
    String baseline1JsonString = "{\n" +
            "\"type\": \"dnorm\",\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";
    String baseline2JsonString = "{\n" +
            "\"type\": \"dnorm\",\n" +
            "\"scale\": \"log odds\",\n" +
            "\"mu\": 4,\n" +
            "\"sigma\": 6,\n" +
            "\"name\": \"fluox\"\n" +
            "}";

    BenefitRiskNMAOutcomeInclusion inclusion1 = new BenefitRiskNMAOutcomeInclusion(analysisId, outcome1.getId(), nma1Id, model1.getId());
    inclusion1.setBaseline(baseline1JsonString);
    BenefitRiskNMAOutcomeInclusion inclusion2 = new BenefitRiskNMAOutcomeInclusion(analysisId, outcome2.getId(), nma2Id, model2.getId());
    inclusion2.setBaseline(baseline2JsonString);
    List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions = Arrays.asList(inclusion1, inclusion2);
    analysis.setBenefitRiskNMAOutcomeInclusions(outcomeInclusions);

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

    Set<Integer> modelIds = Sets.newHashSet(model1.getId(), model2.getId());
    Set<Integer> outcomeIds = Sets.newHashSet(outcome1.getId(), outcome2.getId());
    when(projectRepository.get(projectId)).thenReturn(project);
    when(modelService.get(modelIds)).thenReturn(models);
    when(outcomeRepository.get(projectId, outcomeIds)).thenReturn(outcomes);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    List<URI> taskIds = Arrays.asList(model1.getTaskUrl(), model2.getTaskUrl());
    when(pataviTaskRepository.findByUrls(taskIds)).thenReturn(pataviTasks);
    when(pataviTaskRepository.getResults(anyCollectionOf(PataviTask.class))).thenReturn(results);
    List<AbstractMeasurementEntry> networkPerformance = singletonList(mock(AbstractMeasurementEntry.class));

    when(networkPerformanceTableBuilder.build(any(), any(), any(), any(), any(), any())).thenReturn(networkPerformance);

    BenefitRiskProblem problem = (BenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    verify(projectRepository).get(projectId);
    verify(modelService).get(modelIds);
    verify(outcomeRepository).get(projectId, outcomeIds);
    verify(analysisRepository).get(analysisId);
    verify(pataviTaskRepository).findByUrls(taskIds);
    verify(pataviTaskRepository).getResults(anyCollectionOf(PataviTask.class));
    verify(uuidService, times(outcomeInclusions.size())).generate();
    verify(linkService).getModelSourceLink(project, model1);
    verify(linkService).getModelSourceLink(project, model2);

    assertEquals(3, problem.getAlternatives().size());
    assertEquals(2, problem.getCriteria().size());
    assertEquals(1, problem.getPerformanceTable().size());
  // move to performancetable builder test
    //    assertEquals("relative-normal", problem.getPerformanceTable().get(0).getPerformance().getType());
//    assertEquals("relative-cloglog-normal", problem.getPerformanceTable().get(1).getPerformance().getType());
//    List<List<Double>> expectedDataHam = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
//    RelativePerformanceParameters parameters = (RelativePerformanceParameters) problem.getPerformanceTable().get(0).getPerformance().getParameters();
//    assertEquals(expectedDataHam, parameters.getRelative().getCov().getData());
//    List<List<Double>> expectedDataHeadache = Arrays.asList(Arrays.asList(0.0, 0.0, 0.0), Arrays.asList(0.0, 74.346, 1.9648), Arrays.asList(0.0, 1.9648, 74.837));
//    RelativePerformanceParameters otherParameters = (RelativePerformanceParameters) problem.getPerformanceTable().get(1).getPerformance().getParameters();
//    assertEquals(expectedDataHeadache, otherParameters.getRelative().getCov().getData());
  }

  @Test
  public void applyModelSettingsSensitivity() throws InvalidModelException {
    String studyToOmit = "studyToOmit";
    String studyToLeave = "studyToLeave";
    AbstractNetworkMetaAnalysisProblemEntry toOmit1 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 1, 1, 1);
    AbstractNetworkMetaAnalysisProblemEntry toOmit2 = new RateNetworkMetaAnalysisProblemEntry(studyToOmit, 2, 2, 2);
    AbstractNetworkMetaAnalysisProblemEntry toLeave = new RateNetworkMetaAnalysisProblemEntry(studyToLeave, 3, 3, 3);
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
