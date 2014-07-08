package org.drugis.addis.analyses;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.CriteriaRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.util.WebConstants;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import javax.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 3/5/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class AnalysisControllerTest {

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Inject
  CriteriaRepository criteriaRepository;

  @Inject
  ScenarioRepository scenarioRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account john = new Account(1, "a", "john", "lennon"),
          paul = new Account(2, "a", "paul", "mc cartney"),
          gert = new Account(3, "gert", "Gert", "van Valkenhoef");


  @Before
  public void setUp() {
    reset(accountRepository, analysisRepository, singleStudyBenefitRiskAnalysisRepository,
            networkMetaAnalysisRepository, scenarioRepository, criteriaRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, analysisRepository, singleStudyBenefitRiskAnalysisRepository,
            networkMetaAnalysisRepository);
  }

  @Test
  public void testQueryAnalyses() throws Exception {
    SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = new SingleStudyBenefitRiskAnalysis(1, 1, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(2, 1, "name", null);
    Integer projectId = 1;
    List<AbstractAnalysis> analyses = Arrays.asList(singleStudyBenefitRiskAnalysis, networkMetaAnalysis);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    ResultActions result = mockMvc.perform(get("/projects/1/analyses").principal(user));
    result
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].analysisType", Matchers.notNullValue()));

    verify(analysisRepository).query(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testUnauthorisedAccessFails() throws Exception {
    when(accountRepository.findAccountByUsername("gert")).thenReturn(null);
    mockMvc.perform(get("/projects/1/analyses").principal(user))
            .andExpect(status().isForbidden());
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testCreateSingleStudyBenefitRiskAnalysis() throws Exception {
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(1, 1, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
    when(analysisService.createSingleStudyBenefitRiskAnalysis(gert, analysisCommand)).thenReturn(analysis);
    String body = TestUtils.createJson(analysisCommand);
    mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).createSingleStudyBenefitRiskAnalysis(gert, analysisCommand);
  }

  @Test
  public void testCreateNetworkMetaAnalysis() throws Exception {
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(1, 1, "name");
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", AnalysisType.NETWORK_META_ANALYSIS_LABEL);
    when(analysisService.createNetworkMetaAnalysis(gert, analysisCommand)).thenReturn(analysis);
    String body = TestUtils.createJson(analysisCommand);
    mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).createNetworkMetaAnalysis(gert, analysisCommand);
  }

  @Test(expected = NestedServletException.class)
  public void testCreateUnknownAnalysisTypeFails() throws Exception {
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", "unknown type");
    String body = TestUtils.createJson(analysisCommand);
    try {
      mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.APPLICATION_JSON_UTF8));
    } catch (Exception e) {
      verify(accountRepository).findAccountByUsername("gert");
      throw e;
    }
  }

  @Test
  public void testGetSSBRAnalysis() throws Exception {
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(1, 1, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    Integer projectId = 1;
    when(analysisRepository.get(projectId, analysis.getId())).thenReturn(analysis);
    mockMvc.perform(get("/projects/1/analyses/1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(analysis.getId())))
            .andExpect(jsonPath("$.analysisType", is(AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL)));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysis.getId());
  }

  @Test
  public void testGetNMAnalysis() throws Exception {
    Integer projectId = 1;
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(1, projectId, "testName", Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
    when(analysisRepository.get(projectId, analysis.getId())).thenReturn(analysis);
    ResultActions result = mockMvc.perform(get("/projects/1/analyses/1").principal(user));

    result.andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(analysis.getId())))
            .andExpect(jsonPath("$.analysisType", is(AnalysisType.NETWORK_META_ANALYSIS_LABEL)))
            .andExpect(jsonPath("$.excludedArms", hasSize(0)));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysis.getId());
  }

  @Test
  public void testGetAnalysisWithAProblem() throws Exception {
    String problem = "{\"key\": \"value\"}";
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(1, 1, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST, problem);
    Integer projectId = 1;
    when(analysisRepository.get(projectId, analysis.getId())).thenReturn(analysis);
    mockMvc.perform(get("/projects/1/analyses/1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(analysis.getId())))
            .andExpect(jsonPath("$.problem.key", is("value")));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysis.getId());
  }

  @Test
  public void testUpdateAnalysisWithoutProblem() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    List<Outcome> selectedOutcomes = Arrays.asList(
            new Outcome(1, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
            new Outcome(2, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
            new Outcome(3, projectId, "name", "motivation", new SemanticOutcome("uri", "label"))
    );
    List<Intervention> selectedInterventions = Arrays.asList(
            new Intervention(1, projectId, "name", "motivation", new SemanticIntervention("uri", "label")),
            new Intervention(2, projectId, "name", "motivation", new SemanticIntervention("uri", "label"))
    );
    SingleStudyBenefitRiskAnalysis oldAnalysis = new SingleStudyBenefitRiskAnalysis(1, projectId, "name", selectedOutcomes, selectedInterventions);
    ObjectMapper objectMapper = new ObjectMapper();
    SingleStudyBenefitRiskAnalysis newAnalysis = objectMapper.convertValue(objectMapper.readTree(exampleUpdateSingleStudyBenefitRiskRequestWithoutProblem()), SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(oldAnalysis);
    when(singleStudyBenefitRiskAnalysisRepository.update(gert, newAnalysis)).thenReturn(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
            .content(exampleUpdateSingleStudyBenefitRiskRequestWithoutProblem())
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.selectedOutcomes", hasSize(3)))
            .andExpect(jsonPath("$.selectedInterventions", hasSize(2)));
    verify(analysisRepository).get(projectId, analysisId);
    verify(accountRepository).findAccountByUsername("gert");
    verify(singleStudyBenefitRiskAnalysisRepository).update(gert, newAnalysis);
  }


  @Test
  public void testUpdateAnalysisWithCreateScenario() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    SingleStudyBenefitRiskAnalysis oldAnalysis = new SingleStudyBenefitRiskAnalysis(1, projectId, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    ObjectMapper objectMapper = new ObjectMapper();
    SingleStudyBenefitRiskAnalysis newAnalysis = objectMapper.convertValue(objectMapper.readTree(exampleUpdateSingleStudyBenefitRiskRequestWithProblem()), SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(oldAnalysis);
    when(singleStudyBenefitRiskAnalysisRepository.update(gert, newAnalysis)).thenReturn(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
            .content(exampleUpdateSingleStudyBenefitRiskRequestWithProblem())
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysisId);
    verify(scenarioRepository).create(analysisId, Scenario.DEFAULT_TITLE, "{\"problem\":" + newAnalysis.getProblem() + "}");
    verify(singleStudyBenefitRiskAnalysisRepository).update(gert, newAnalysis);
  }

  @Test
  public void testUpdateLockedAnalysisFails() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    SingleStudyBenefitRiskAnalysis oldAnalysis = new SingleStudyBenefitRiskAnalysis(1, projectId, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST, "oldProblem");
    ObjectMapper objectMapper = new ObjectMapper();
    SingleStudyBenefitRiskAnalysis newAnalysis = objectMapper.convertValue(objectMapper.readTree(exampleUpdateSingleStudyBenefitRiskRequestWithProblem()), SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(oldAnalysis);
    when(singleStudyBenefitRiskAnalysisRepository.update(gert, newAnalysis)).thenReturn(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
            .content(exampleUpdateSingleStudyBenefitRiskRequestWithProblem())
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isForbidden());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysisId);
  }

  @Test
  public void testUpdate() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    SingleStudyBenefitRiskAnalysis oldAnalysis = new SingleStudyBenefitRiskAnalysis(1, projectId, "name", Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
    ObjectMapper objectMapper = new ObjectMapper();
    SingleStudyBenefitRiskAnalysis newAnalysis = objectMapper.convertValue(objectMapper.readTree(exampleUpdateSingleStudyBenefitRiskRequestWithProblem()), SingleStudyBenefitRiskAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(oldAnalysis);
    when(singleStudyBenefitRiskAnalysisRepository.update(gert, newAnalysis)).thenReturn(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", 1, 1)
            .content(exampleUpdateSingleStudyBenefitRiskRequestWithProblem())
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysisId);
    verify(singleStudyBenefitRiskAnalysisRepository).update(gert, newAnalysis);
  }

  @Test
  public void testUpdateNetworkMetaAnalysis() throws Exception {
    Integer analysisId = 333;
    Integer projectId = 101;
    Integer outcomeId = 444;
    NetworkMetaAnalysis oldAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis name");
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", "motivation", new SemanticOutcome("uir", "label"));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(oldAnalysis.getId(), oldAnalysis.getProjectId(), oldAnalysis.getName(), outcome);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(oldAnalysis);
    when(analysisService.updateNetworkMetaAnalysis(gert, newAnalysis)).thenReturn(newAnalysis);
    String jsonCommand = TestUtils.createJson(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
            .content(jsonCommand)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testUpdateWithExcludedArms() throws Exception {
    Integer analysisId = 333;
    Integer projectId = 101;
    Integer outcomeId = 444;
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", "motivation", new SemanticOutcome("uir", "label"));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "name", new ArrayList<ArmExclusion>(), Collections.EMPTY_LIST, outcome);
    newAnalysis.getExcludedArms().addAll(Arrays.asList(new ArmExclusion(null, -1L), new ArmExclusion(null, -2L)));

    String jsonCommand = TestUtils.createJson(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
            .content(jsonCommand)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testUpdateWithIncludedInterventions() throws Exception {
    Integer analysisId = 333;
    Integer projectId = 101;
    Integer outcomeId = 444;
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", "motivation", new SemanticOutcome("uir", "label"));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "name", Collections.EMPTY_LIST, new ArrayList<InterventionInclusion>(), outcome);
    newAnalysis.getIncludedInterventions().addAll(Arrays.asList(new InterventionInclusion(null, -1), new InterventionInclusion(null, -2)));

    String jsonCommand = TestUtils.createJson(newAnalysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
      .content(jsonCommand)
      .principal(user)
      .contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }


  String exampleUpdateSingleStudyBenefitRiskRequestWithProblem() {
    return TestUtils.loadResource(this.getClass(), "/analysisController/exampleSingleStudyBenefitRiskAnalysisWithProblem.json");
  }

  String exampleUpdateSingleStudyBenefitRiskRequestWithoutProblem() {
    return TestUtils.loadResource(this.getClass(), "/analysisController/exampleSingleStudyBenefitRiskAnalysisWithoutProblem.json");
  }

  String exampleUpdateNetworkMetaAnalysisRequestWithProblem() {
    return TestUtils.loadResource(this.getClass(), "/analysisController/exampleNetworkMetaAnalysisWithProblem.json");
  }

}
