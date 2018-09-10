package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import net.minidev.json.JSONObject;
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
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
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
  private ModelService modelService;

  @Mock
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Mock
  private OutcomeRepository outcomeRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LinkService linkService;

  @Mock
  private SingleStudyBenefitRiskService singleStudyBenefitRiskService;

  @Mock
  private NetworkBenefitRiskPerformanceEntryBuilder networkBenefitRiskPerformanceEntryBuilder;

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
  private final Set<AbstractIntervention> allProjectInterventions = newHashSet(fluoxIntervention, paroxIntervention, sertraIntervention);

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
        modelService, singleStudyBenefitRiskService,
        networkMetaAnalysisService, linkService,
        networkBenefitRiskPerformanceEntryBuilder
    );
  }

  @Test
  public void testGetProblemSingleStudyBenefitRisk() throws Exception, ProblemCreationException {
    // prepare outcomes
    URI secondOutcomeUri = URI.create("http://secondSemantic");
    SemanticVariable secondSemanticOutcome = new SemanticVariable(secondOutcomeUri, "second semantic outcome");
    Outcome secondOutcome = new Outcome(-303, projectId, "second outcome", direction, "very", secondSemanticOutcome);
    List<Outcome> outcomes = Arrays.asList(outcome, secondOutcome);
    when(outcomeRepository.get(projectId, newHashSet(outcome.getId(), secondOutcome.getId()))).thenReturn(outcomes);

    URI defaultMeasurementMoment = URI.create("defaultMeasurementMoment");
    TrialDataStudy studyMock = mock(TrialDataStudy.class);
    when(studyMock.getDefaultMeasurementMoment()).thenReturn(defaultMeasurementMoment);
    URI daanEtAlUri = URI.create("daanEtAl");
    SingleStudyContext context = mock(SingleStudyContext.class);

    Set<InterventionInclusion> interventionInclusions = buildInterventionInclusions();

    // build analysis & return it
    BenefitRiskAnalysis singleStudyAnalysis = new BenefitRiskAnalysis(analysisId, projectId, "single study analysis", interventionInclusions);
    BenefitRiskStudyOutcomeInclusion outcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, outcome.getId(), daanEtAlUri);
    BenefitRiskStudyOutcomeInclusion secondOutcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, secondOutcome.getId(), daanEtAlUri);
    singleStudyAnalysis.setBenefitRiskStudyOutcomeInclusions(Arrays.asList(outcomeInclusion, secondOutcomeInclusion));
    when(analysisRepository.get(analysisId)).thenReturn(singleStudyAnalysis);

    // prepare interventions for retrieval
    Set<AbstractIntervention> includedInterventions = newHashSet(fluoxIntervention, sertraIntervention);
    Set<SingleIntervention> singleInterventions = newHashSet(fluoxIntervention, sertraIntervention);
    when(analysisService.getIncludedInterventions(singleStudyAnalysis)).thenReturn(includedInterventions);
    when(analysisService.getSingleInterventions(allProjectInterventions)).thenReturn(singleInterventions);
    when(analysisService.getSingleInterventions(includedInterventions)).thenReturn(singleInterventions);

    // prepare single study BR service
    when(singleStudyBenefitRiskService.buildContext(project, daanEtAlUri, newHashSet(outcomes), includedInterventions)).thenReturn(context);
    when(singleStudyBenefitRiskService.getSingleStudyMeasurements(project, daanEtAlUri, context)).thenReturn(studyMock);
    List<TrialDataArm> armsMock = Arrays.asList(mock(TrialDataArm.class), mock(TrialDataArm.class));
    when(singleStudyBenefitRiskService.getArmsWithMatching(includedInterventions, studyMock.getTrialDataArms())).thenReturn(armsMock);
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

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);

    verify(singleStudyBenefitRiskService).buildContext(project, daanEtAlUri, newHashSet(outcomes), includedInterventions);
    verify(singleStudyBenefitRiskService).getSingleStudyMeasurements(project, daanEtAlUri, context);
    verify(singleStudyBenefitRiskService).getArmsWithMatching(includedInterventions, studyMock.getTrialDataArms());
    verify(singleStudyBenefitRiskService).getCriteria(armsMock, defaultMeasurementMoment, context);
    verify(singleStudyBenefitRiskService).getAlternatives(armsMock, context);
    verify(singleStudyBenefitRiskService).getMeasurementsWithCoordinates(armsMock, defaultMeasurementMoment, context);
    verify(singleStudyBenefitRiskService).buildPerformanceTable(measurementsMock);
  }

  private Set<InterventionInclusion> buildInterventionInclusions() {
    InterventionInclusion fluoxInclusion = new InterventionInclusion(analysisId, fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(analysisId, sertraIntervention.getId());
    return newHashSet(fluoxInclusion, sertraInclusion);
  }

  @Test
  public void testGetProblemNMA() throws URISyntaxException, ReadValueException, ResourceDoesNotExistException, ProblemCreationException, IOException {

    // analysis
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);

    List<TreatmentEntry> treatments = singletonList(mock(TreatmentEntry.class));
    List<TrialDataStudy> studies = singletonList(mock(TrialDataStudy.class));
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = singletonList(mock(AbstractNetworkMetaAnalysisProblemEntry.class));
    List<TrialDataStudy> studiesWithEntries = singletonList(mock(TrialDataStudy.class));

    Map<String, Map<String, Double>> studyLevelCovariates = new HashMap<>();
    Map<String, Double> covariateValues = new HashMap<>();
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
  public void testGetProblemNMABR() throws Exception, ProblemCreationException {

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
    when(projectRepository.get(projectId)).thenReturn(project);

    Set<InterventionInclusion> interventionInclusions = buildInterventionInclusions();

    Set<Integer> outcomeIds = singleton(outcome.getId());
    when(outcomeRepository.get(projectId, outcomeIds)).thenReturn(singletonList(outcome));

    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, title, interventionInclusions);
    Integer nmaId = 3;
    Integer modelId = 15;
    BenefitRiskNMAOutcomeInclusion nmaInclusion = new BenefitRiskNMAOutcomeInclusion(analysisId, outcome.getId(), nmaId, modelId);
    nmaInclusion.setBaseline("");
    int notIncludedModelId = 823;
    BenefitRiskNMAOutcomeInclusion nmaInclusionNoBaseline = new BenefitRiskNMAOutcomeInclusion(analysisId, outcome.getId(), nmaId, notIncludedModelId);
    BenefitRiskNMAOutcomeInclusion nmaInclusionNoModel = new BenefitRiskNMAOutcomeInclusion(analysisId, outcome.getId(), nmaId, null);
    analysis.setBenefitRiskNMAOutcomeInclusions(Arrays.asList(nmaInclusion, nmaInclusionNoBaseline, nmaInclusionNoModel));
    when(analysisRepository.get(analysisId)).thenReturn(analysis);

    Model model = mock(Model.class);
    when(model.getId()).thenReturn(modelId);
    Set<Integer> modelIds = newHashSet(modelId, notIncludedModelId);
    when(modelService.get(modelIds)).thenReturn(singletonList(model));

    Set<AbstractIntervention> interventions = newHashSet(fluoxIntervention, paroxIntervention);
    when(analysisService.getIncludedInterventions(analysis)).thenReturn(interventions);

    Map<Integer, JsonNode> resultsByModelId = new HashMap<>();
    resultsByModelId.put(modelId, mock(JsonNode.class));
    when(networkMetaAnalysisService.getPataviResultsByModelId(Sets.newHashSet(model))).thenReturn(resultsByModelId);

    URI modelSourceLink = URI.create("modelSourceLink");
    when(linkService.getModelSourceLink(project, model)).thenReturn(modelSourceLink);

    NMAInclusionWithResults inclusionWithResults = new NMAInclusionWithResults(outcome, model,
        resultsByModelId.get(modelId), interventions, "");
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    CriterionEntry criterionEntry = mock(CriterionEntry.class);
    DataSourceEntry dataSourceEntry = new DataSourceEntry("1", "source", modelSourceLink);
    when(criterionEntry.getDataSources()).thenReturn(singletonList(dataSourceEntry));
    criteria.put(outcome.getSemanticOutcomeUri(), criterionEntry);
    when(networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelSourceLink)).thenReturn(criteria);

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    alternatives.put(fluoxConceptUri.toString(), mock(AlternativeEntry.class));
    when(networkMetaAnalysisService.buildAlternativesForInclusion(inclusionWithResults)).thenReturn(alternatives);

    AbstractMeasurementEntry measurementEntry = mock(AbstractMeasurementEntry.class);
    when(networkBenefitRiskPerformanceEntryBuilder.build(inclusionWithResults, dataSourceEntry)).thenReturn(measurementEntry);

    //execute
    BenefitRiskProblem result = (BenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    BenefitRiskProblem expectedResult = new BenefitRiskProblem(WebConstants.SCHEMA_VERSION, criteria, alternatives, singletonList(measurementEntry));
    assertEquals(expectedResult, result);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(modelService).get(modelIds);
    verify(analysisService).getIncludedInterventions(analysis);
    verify(linkService).getModelSourceLink(project, model);
    verify(networkMetaAnalysisService).getPataviResultsByModelId(Sets.newHashSet(model));
    verify(networkMetaAnalysisService).buildCriteriaForInclusion(inclusionWithResults, modelSourceLink);
    verify(networkMetaAnalysisService).buildAlternativesForInclusion(inclusionWithResults);
    verify(networkBenefitRiskPerformanceEntryBuilder).build(inclusionWithResults, dataSourceEntry);

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
