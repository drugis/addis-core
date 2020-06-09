package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.ModelBaseline;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.NetworkBenefitRiskServiceImpl;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NetworkBenefitRiskServiceImplTest {

  @Mock
  private LinkService linkService;

  @Mock
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Mock
  private NetworkBenefitRiskPerformanceEntryBuilder networkBenefitRiskPerformanceEntryBuilder;

  @InjectMocks
  private NetworkBenefitRiskService networkBenefitRiskService;

  private Integer analysisId = 1;
  private Integer outcomeId = 10;
  private Integer networkMetaAnalysisId = 100;
  private Integer modelId = 1000;
  private String baselineString = "{ name: 'interventionName' }";
  private AbstractIntervention intervention = mock(AbstractIntervention.class);
  private Set<AbstractIntervention> includedInterventions = new HashSet<>(Collections.singletonList(intervention));
  private JsonNode pataviResults = mock(JsonNode.class);
  private ModelBaseline baseline;
  private BenefitRiskNMAOutcomeInclusion inclusion;
  private Model model;
  private Map<Integer, Model> modelsById;
  private Map<Integer, JsonNode> resultsByModelId;

  @Before
  public void init() {
    networkBenefitRiskService = new NetworkBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
    inclusion = new BenefitRiskNMAOutcomeInclusion(analysisId, outcomeId, networkMetaAnalysisId, modelId);
    model = mock(Model.class);
    modelsById = new HashMap<>();
    modelsById.put(modelId, model);
    baseline = mock(ModelBaseline.class);
    resultsByModelId = new HashMap<>();
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(
            linkService,
            networkMetaAnalysisService,
            networkBenefitRiskPerformanceEntryBuilder
    );
  }

  @Test
  public void inclusionHasBaseline() {
    inclusion.setBaselineThroughString(baselineString);
    boolean result = networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions);
    assertTrue(result);
  }

  @Test
  public void inclusionHasNoModel() {
    Integer modelId = null;
    BenefitRiskNMAOutcomeInclusion inclusion = new BenefitRiskNMAOutcomeInclusion(analysisId, outcomeId, networkMetaAnalysisId, modelId);
    boolean result = networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions);
    assertFalse(result);
  }

  @Test
  public void inclusionModelHasNoBaseline() {
    when(model.getBaseline()).thenReturn(null);
    boolean result = networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions);
    assertFalse(result);
  }

  @Test
  public void inclusionNoMatchingIntervention() {
    when(model.getBaseline()).thenReturn(baseline);
    when(baseline.getBaseline()).thenReturn(baselineString);
    when(intervention.getName()).thenReturn("notInterventionName");
    boolean result = networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions);
    assertFalse(result);
  }

  @Test
  public void inclusionWithMatchingIntervention() {
    when(model.getBaseline()).thenReturn(baseline);
    when(baseline.getBaseline()).thenReturn(baselineString);
    when(intervention.getName()).thenReturn("interventionName");
    boolean result = networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions);
    assertTrue(result);
  }

  @Test
  public void hasNoResults() {
    boolean result = networkBenefitRiskService.hasResults(resultsByModelId, inclusion);
    assertFalse(result);
  }

  @Test
  public void hasResults() {
    JsonNode modelResult = mock(JsonNode.class);
    resultsByModelId.put(modelId, modelResult);
    boolean result = networkBenefitRiskService.hasResults(resultsByModelId, inclusion);
    assertTrue(result);
  }

  @Test
  public void getNmaInclusionWithResultsBaselineFromModel() {
    Map<Integer, Outcome> outcomesById = new HashMap<>();
    Outcome outcome = mock(Outcome.class);
    outcomesById.put(outcomeId, outcome);
    resultsByModelId.put(modelId, pataviResults);

    when(model.getBaseline()).thenReturn(baseline);
    when(baseline.getBaseline()).thenReturn(baselineString);

    NMAInclusionWithResults result = networkBenefitRiskService.getNmaInclusionWithResults(
            outcomesById,
            includedInterventions,
            modelsById,
            resultsByModelId,
            inclusion
    );

    NMAInclusionWithResults expectedResult = new NMAInclusionWithResults(outcome, model, pataviResults, includedInterventions, baselineString);
    assertEquals(expectedResult, result);
  }

  @Test
  public void getNmaInclusionWithResultsBaselineFromInclusion() {
    Map<Integer, Outcome> outcomesById = new HashMap<>();
    Outcome outcome = mock(Outcome.class);
    outcomesById.put(outcomeId, outcome);
    resultsByModelId.put(modelId, pataviResults);
    inclusion.setBaselineThroughString(baselineString);
    NMAInclusionWithResults result = networkBenefitRiskService.getNmaInclusionWithResults(
            outcomesById,
            includedInterventions,
            modelsById,
            resultsByModelId,
            inclusion
    );

    NMAInclusionWithResults expectedResult = new NMAInclusionWithResults(outcome, model, pataviResults, includedInterventions, baselineString);
    assertEquals(expectedResult, result);
  }

  @Test
  public void getNetworkProblem() throws URISyntaxException {
    Project project = mock(Project.class);
    NMAInclusionWithResults inclusionWithResults = mock(NMAInclusionWithResults.class);
    URI modelURI = new URI("uri.com");
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    DataSourceEntry dataSource = mock(DataSourceEntry.class);
    List<DataSourceEntry> dataSources = Collections.singletonList(dataSource);
    CriterionEntry criterion = new CriterionEntry(dataSources, "criterion");
    criteria.put(modelURI, criterion);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    AbstractMeasurementEntry relativePerformance = mock(AbstractMeasurementEntry.class);

    when(inclusionWithResults.getModel()).thenReturn(model);
    when(linkService.getModelSourceLink(project, model)).thenReturn(modelURI);
    when(networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelURI)).thenReturn(criteria);
    when(networkMetaAnalysisService.buildAlternativesForInclusion(inclusionWithResults)).thenReturn(alternatives);
    when(networkBenefitRiskPerformanceEntryBuilder.build(inclusionWithResults, dataSource)).thenReturn(relativePerformance);

    BenefitRiskProblem result = networkBenefitRiskService.getNetworkProblem(project, inclusionWithResults);

    BenefitRiskProblem expectedResult = new BenefitRiskProblem(criteria, alternatives, Collections.singletonList(relativePerformance));
    assertEquals(expectedResult, result);

    verify(linkService).getModelSourceLink(project, model);
    verify(networkMetaAnalysisService).buildCriteriaForInclusion(inclusionWithResults, modelURI);
    verify(networkMetaAnalysisService).buildAlternativesForInclusion(inclusionWithResults);
    verify(networkBenefitRiskPerformanceEntryBuilder).build(inclusionWithResults, dataSource);
  }
}