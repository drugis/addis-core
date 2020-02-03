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
import org.drugis.addis.problems.model.problemEntry.AbsoluteDichotomousProblemEntry;
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;
import org.drugis.addis.problems.service.impl.ProblemServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.Namespaces;
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
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


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
  private SingleStudyBenefitRiskService singleStudyBenefitRiskService;

  @Mock
  private NetworkBenefitRiskService networkBenefitRiskService;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private VersionMappingRepository versionMappingRepository;

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
  private final URI sertraConceptUri = URI.create("sertraConceptUri");
  private final SemanticInterventionUriAndName sertraConcept = new SemanticInterventionUriAndName(sertraConceptUri, "sertra concept");
  private final Integer sertraInterventionId = 403;
  private final SingleIntervention sertraIntervention = new SimpleIntervention(sertraInterventionId, project.getId(),
          "sertraline", "motivation", sertraConcept.getUri(), sertraConcept.getLabel());
  private String studyToOmit = "studyToOmit";

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
    verifyNoMoreInteractions(
            analysisRepository,
            projectRepository,
            modelService,
            singleStudyBenefitRiskService,
            networkMetaAnalysisService,
            networkBenefitRiskService,
            triplestoreService,
            versionMappingRepository
    );
  }

  @Test
  public void testGetProblemSingleStudyBenefitRisk() throws Exception, ProblemCreationException {
    Set<InterventionInclusion> interventionInclusions = buildInterventionInclusions();
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, "single study analysis", interventionInclusions);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);

    URI defaultMeasurementMoment = URI.create("defaultMeasurementMoment");
    TrialDataStudy studyMock = mock(TrialDataStudy.class);
    when(studyMock.getDefaultMeasurementMoment()).thenReturn(defaultMeasurementMoment);

    URI studyUri1 = URI.create("daanEtAl");
    URI studyUri2 = URI.create("JorisEtAl");
    String source1 = "source 1";
    String source2 = "source 2";
    Map sources = mock(Map.class);
    when(sources.get(studyUri1)).thenReturn(source1);
    when(sources.get(studyUri2)).thenReturn(source2);

    Integer secondOutcomeId = 1337;
    URI secondOutcomeUri = URI.create("http://secondSemantic");
    SemanticVariable secondSemanticOutcome = new SemanticVariable(secondOutcomeUri, "second semantic outcome");
    Outcome secondOutcome = new Outcome(secondOutcomeId, projectId, "second outcome", direction, "very", secondSemanticOutcome);
    List<Outcome> outcomes = Arrays.asList(secondOutcome, outcome);
    when(outcomeRepository.get(
            projectId,
            newHashSet(outcome.getId(), secondOutcome.getId())
    )).thenReturn(outcomes);

    List<BenefitRiskStudyOutcomeInclusion> benefitRiskStudyOutcomeInclusions =
            getBenefitRiskStudyOutcomeInclusions(
                    secondOutcomeId,
                    studyUri1,
                    studyUri2
            );
    analysis.setBenefitRiskStudyOutcomeInclusions(benefitRiskStudyOutcomeInclusions);

    Set<AbstractIntervention> includedInterventions = newHashSet(fluoxIntervention, sertraIntervention);
    when(analysisService.getIncludedInterventions(analysis)).thenReturn(includedInterventions);

    Map<URI, CriterionEntry> criteriaMock = getCriteriaMock();
    Map<String, AlternativeEntry> alternativesMock = getAlternativesMock();
    List<AbstractMeasurementEntry> performanceMock = singletonList(mock(AbstractMeasurementEntry.class));

    SingleStudyBenefitRiskProblem singleStudyProblemMock1 = mock(SingleStudyBenefitRiskProblem.class);
    SingleStudyBenefitRiskProblem singleStudyProblemMock2 = mock(SingleStudyBenefitRiskProblem.class);
    when(singleStudyProblemMock1.getCriteria()).thenReturn(criteriaMock);
    when(singleStudyProblemMock1.getAlternatives()).thenReturn(alternativesMock);
    when(singleStudyProblemMock1.getPerformanceTable()).thenReturn(performanceMock);
    when(singleStudyProblemMock2.getCriteria()).thenReturn(criteriaMock);
    when(singleStudyProblemMock2.getAlternatives()).thenReturn(alternativesMock);
    when(singleStudyProblemMock2.getPerformanceTable()).thenReturn(performanceMock);
    when(singleStudyBenefitRiskService.getSingleStudyBenefitRiskProblem(
            project,
            benefitRiskStudyOutcomeInclusions.get(0),
            secondOutcome,
            includedInterventions,
            source2)
    ).thenReturn(singleStudyProblemMock1);
    when(singleStudyBenefitRiskService.getSingleStudyBenefitRiskProblem(
            project,
            benefitRiskStudyOutcomeInclusions.get(1),
            outcome,
            includedInterventions,
            source1)
    ).thenReturn(singleStudyProblemMock2);

    String tripleStoreUid = "triple store uid";
    VersionMapping versionMappingMock = mock(VersionMapping.class);
    when(versionMappingMock.getVersionedDatasetUrl()).thenReturn("/datasets/" + tripleStoreUid);
    URI trialverseDatasetUrl = URI.create(Namespaces.DATASET_NAMESPACE + projectDatasetUid);
    when(versionMappingRepository
            .getVersionMappingByDatasetUrl(
                    trialverseDatasetUrl))
            .thenReturn(versionMappingMock);
    when(triplestoreService.getStudyTitlesByUri(tripleStoreUid, projectDatasetVersion)).thenReturn(sources);

    // --------------- execute ---------------- //
    BenefitRiskProblem result = (BenefitRiskProblem) problemService.getProblem(projectId, analysisId);
    // --------------- execute ---------------- //

    List<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    performanceTable.addAll(performanceMock);
    performanceTable.addAll(performanceMock);
    BenefitRiskProblem expectedResult = new BenefitRiskProblem(
            WebConstants.SCHEMA_VERSION, criteriaMock, alternativesMock, performanceTable);

    assertEquals(expectedResult, result);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(analysisService).getIncludedInterventions(analysis);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(trialverseDatasetUrl);
    verify(triplestoreService).getStudyTitlesByUri(tripleStoreUid, projectDatasetVersion);
    verify(singleStudyBenefitRiskService).getSingleStudyBenefitRiskProblem(
            project,
            benefitRiskStudyOutcomeInclusions.get(0),
            secondOutcome,
            includedInterventions,
            source2
    );
    verify(singleStudyBenefitRiskService).getSingleStudyBenefitRiskProblem(
            project,
            benefitRiskStudyOutcomeInclusions.get(1),
            outcome,
            includedInterventions,
            source1
    );
  }

  private Map<String, AlternativeEntry> getAlternativesMock() {
    Map<String, AlternativeEntry> alternativesMock = new HashMap<>();
    alternativesMock.put(String.valueOf(fluoxInterventionId), mock(AlternativeEntry.class));
    alternativesMock.put(String.valueOf(sertraInterventionId), mock(AlternativeEntry.class));
    return alternativesMock;
  }

  private Map<URI, CriterionEntry> getCriteriaMock() {
    Map<URI, CriterionEntry> criteriaMock = new HashMap<>();
    criteriaMock.put(URI.create("criterion1"), mock(CriterionEntry.class));
    return criteriaMock;
  }

  private List<BenefitRiskStudyOutcomeInclusion> getBenefitRiskStudyOutcomeInclusions(Integer secondOutcomeId, URI studyUri1, URI studyUri2) {
    BenefitRiskStudyOutcomeInclusion outcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, outcome.getId(), studyUri1);
    BenefitRiskStudyOutcomeInclusion secondOutcomeInclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, secondOutcomeId, studyUri2);
    return Arrays.asList(secondOutcomeInclusion, outcomeInclusion);
  }

  private Set<InterventionInclusion> buildInterventionInclusions() {
    InterventionInclusion fluoxInclusion = new InterventionInclusion(analysisId, fluoxIntervention.getId());
    InterventionInclusion sertraInclusion = new InterventionInclusion(analysisId, sertraIntervention.getId());
    return newHashSet(fluoxInclusion, sertraInclusion);
  }

  @Test
  public void testGetProblemNMA() throws URISyntaxException, ReadValueException, ResourceDoesNotExistException, ProblemCreationException, IOException {
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, project.getId(), "nma title", outcome);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);

    List<TreatmentEntry> treatments = singletonList(mock(TreatmentEntry.class));
    List<TrialDataStudy> studies = singletonList(mock(TrialDataStudy.class));
    List<AbstractProblemEntry> entries = singletonList(mock(AbstractProblemEntry.class));
    List<TrialDataStudy> studiesWithEntries = singletonList(mock(TrialDataStudy.class));

    Map<String, Map<String, Double>> studyLevelCovariates = new HashMap<>();
    Map<String, Double> covariateValues = new HashMap<>();
    covariateValues.put("covariateName", 1.0);
    studyLevelCovariates.put("studyName", covariateValues);

    when(networkMetaAnalysisService.getTreatments(analysis)).thenReturn(treatments);
    when(analysisService.buildEvidenceTable(project.getId(), analysis.getId())).thenReturn(studies);
    when(networkMetaAnalysisService.buildAbsolutePerformanceEntries(analysis, studies)).thenReturn(entries);
    when(networkMetaAnalysisService.buildRelativeEffectData(analysis, studies)).thenReturn(new RelativeEffectData());
    when(networkMetaAnalysisService.getStudiesWithEntries(studies, entries)).thenReturn(studiesWithEntries);
    when(networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studiesWithEntries)).thenReturn(studyLevelCovariates);

    final AbstractProblem result = problemService.getProblem(project.getId(), analysis.getId());

    NetworkMetaAnalysisProblem expectedResult = new NetworkMetaAnalysisProblem(
            entries,
            treatments,
            studyLevelCovariates
    );
    assertEquals(expectedResult, result);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(networkMetaAnalysisService).getTreatments(analysis);
    verify(analysisService).buildEvidenceTable(project.getId(), analysis.getId());
    verify(networkMetaAnalysisService).buildAbsolutePerformanceEntries(analysis, studies);
    verify(networkMetaAnalysisService).buildRelativeEffectData(analysis, studies);
    verify(networkMetaAnalysisService).getStudiesWithEntries(studies, entries);
    verify(networkMetaAnalysisService).getStudyLevelCovariates(project, analysis, studiesWithEntries);
  }

  @Test
  public void testGetProblemNMABR() throws Exception, ProblemCreationException {
    BenefitRiskAnalysis analysis = mock(BenefitRiskAnalysis.class);
    Integer outcomeId1 = 1;
    Integer nmaId1 = 10;
    Integer modelId1 = 100;
    Model model1 = mock(Model.class);
    BenefitRiskNMAOutcomeInclusion outcomeInclusion1 = new BenefitRiskNMAOutcomeInclusion(analysisId, outcomeId1, nmaId1, modelId1);
    List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions = Collections.singletonList(outcomeInclusion1);
    Set<Integer> outcomeIds = new HashSet<>(Collections.singletonList(outcomeId1));
    Outcome outcome1 = mock(Outcome.class);
    List<Outcome> outcomes = Collections.singletonList(outcome1);
    AbstractIntervention intervention1 = mock(AbstractIntervention.class);
    AbstractIntervention intervention2 = mock(AbstractIntervention.class);
    Set<AbstractIntervention> includedInterventions = new HashSet<>(Arrays.asList(intervention1, intervention2));
    Set<Integer> modelIds = new HashSet<>(Collections.singletonList(modelId1));
    List<Model> models = Collections.singletonList(model1);
    Collection<Model> modelsSet = Sets.newHashSet(models);
    Map<Integer, JsonNode> resultsByModelId = new HashMap<>();
    BenefitRiskProblem networkProblem = mock(BenefitRiskProblem.class);
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    List<AbstractMeasurementEntry> performanceTable = Collections.emptyList();

    when(projectRepository.get(projectId)).thenReturn(project);
    when(analysisRepository.get(analysisId)).thenReturn(analysis);
    when(analysis.getBenefitRiskNMAOutcomeInclusions()).thenReturn(outcomeInclusions);
    when(analysis.getBenefitRiskStudyOutcomeInclusions()).thenReturn(Collections.emptyList());
    when(outcomeRepository.get(projectId, outcomeIds)).thenReturn(outcomes);
    when(outcome1.getId()).thenReturn(outcomeId1);
    when(analysisService.getIncludedInterventions(analysis)).thenReturn(includedInterventions);
    when(modelService.get(modelIds)).thenReturn(models);
    when(model1.getId()).thenReturn(modelId1);
    when(networkMetaAnalysisService.getPataviResultsByModelId(modelsSet)).thenReturn(resultsByModelId);
    when(networkBenefitRiskService.hasBaseline(any(), any(), any())).thenReturn(true);
    when(networkBenefitRiskService.hasResults(any(), any())).thenReturn(true);
    when(networkBenefitRiskService.getNmaInclusionWithResults(any(), any(), any(), any(), any())).thenReturn(null);
    when(networkBenefitRiskService.getNetworkProblem(any(), any())).thenReturn(networkProblem);
    when(networkProblem.getCriteria()).thenReturn(criteria);
    when(networkProblem.getAlternatives()).thenReturn(alternatives);
    when(networkProblem.getPerformanceTable()).thenReturn(performanceTable);
    VersionMapping mappingMock = mock(VersionMapping.class);
    when(mappingMock.getVersionedDatasetUrl()).thenReturn("/datasets/something");
    when(versionMappingRepository.getVersionMappingByDatasetUrl(any())).thenReturn(mappingMock);
    when(triplestoreService.getStudyTitlesByUri(any(), any())).thenReturn(null);

    BenefitRiskProblem result = (BenefitRiskProblem) problemService.getProblem(projectId, analysisId);

    BenefitRiskProblem expectedResult = new BenefitRiskProblem(
            WebConstants.SCHEMA_VERSION,
            criteria,
            alternatives,
            performanceTable
    );
    assertEquals(expectedResult, result);

    verify(projectRepository).get(projectId);
    verify(analysisRepository).get(analysisId);
    verify(outcomeRepository).get(projectId, outcomeIds);
    verify(analysisService).getIncludedInterventions(analysis);
    verify(modelService).get(modelIds);
    verify(networkMetaAnalysisService).getPataviResultsByModelId(modelsSet);
    verify(networkBenefitRiskService).hasBaseline(any(), any(), any());
    verify(networkBenefitRiskService).hasResults(any(), any());
    verify(networkBenefitRiskService).getNmaInclusionWithResults(any(), any(), any(), any(), any());
    verify(networkBenefitRiskService).getNetworkProblem(any(), any());
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(any());
    verify(triplestoreService).getStudyTitlesByUri(any(), any());
  }

  @Test
  public void applyModelSettingsSensitivity() throws InvalidModelException {
    String studyToLeave = "studyToLeave";
    AbstractProblemEntry toOmit1 = new AbsoluteDichotomousProblemEntry(studyToOmit, 1, 1, 1);
    AbstractProblemEntry toOmit2 = new AbsoluteDichotomousProblemEntry(studyToOmit, 2, 2, 2);
    AbstractProblemEntry toLeave = new AbsoluteDichotomousProblemEntry(studyToLeave, 3, 3, 3);
    List<AbstractProblemEntry> entries = Arrays.asList(toOmit1, toOmit2, toLeave);
    NetworkMetaAnalysisProblem result = getNetworkMetaAnalysisProblem(studyToOmit, entries);
    assertEquals(1, result.getEntries().size());
    assertFalse(result.getEntries().contains(toOmit1));
    assertFalse(result.getEntries().contains(toOmit2));
    assertTrue(result.getEntries().contains(toLeave));
  }

  @Test
  public void applyModelSettingsNoSensitivity() throws InvalidModelException {
    AbstractProblemEntry entry1 = new AbsoluteDichotomousProblemEntry(studyToOmit, 1, 1, 1);
    AbstractProblemEntry entry2 = new AbsoluteDichotomousProblemEntry(studyToOmit, 2, 2, 2);
    List<AbstractProblemEntry> entries = Arrays.asList(entry1, entry2);
    NetworkMetaAnalysisProblem result = getNetworkMetaAnalysisProblem(studyToOmit, entries);
    assertEquals(Collections.emptyList(), result.getEntries());
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(
          String studyToOmit,
          List<AbstractProblemEntry> entries
  ) throws InvalidModelException {
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
    return problemService.applyModelSettings(problem, model);
  }

}
