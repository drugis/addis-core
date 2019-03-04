package org.drugis.addis.analyses;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.controller.AnalysisUpdateCommand;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.CriteriaRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
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
import java.net.URI;
import java.security.Principal;
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

  @Inject
  private CriteriaRepository criteriaRepository;
  @Inject
  private SubProblemService subProblemService;
  private MockMvc mockMvc;
  @Inject
  private AccountRepository accountRepository;
  @Inject
  private AnalysisRepository analysisRepository;
  @Inject
  private AnalysisService analysisService;
  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;
  @Inject
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;
  @Inject
  private WebApplicationContext webApplicationContext;
  @Inject
  private ProjectService projectService;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  private URI uri = URI.create("uri");
  private Integer projectId = 1;
  private Integer analysisId = 1;

  @Before
  public void setUp() {
    reset(accountRepository, analysisRepository,
        networkMetaAnalysisRepository, subProblemService, criteriaRepository, analysisService, projectService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, analysisRepository,
        networkMetaAnalysisRepository, analysisService, criteriaRepository, subProblemService, projectService);
  }

  @Test
  public void testQueryAnalyses() throws Exception {
    BenefitRiskAnalysis benefitRiskAnalysis = new BenefitRiskAnalysis(1, 1, "name", Collections.emptySet());
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(2, 1, "name", null);
    List<AbstractAnalysis> analyses = Arrays.asList(benefitRiskAnalysis, networkMetaAnalysis);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    ResultActions result = mockMvc.perform(get("/projects/1/analyses"));
    result
        .andExpect(status().isOk())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].analysisType", Matchers.notNullValue()));

    verify(analysisRepository).query(projectId);
  }

  @Test
  public void testQueryNetworkMetaAnalysisByOutcomes() throws Exception {
    Integer direction = 1;
    Outcome outcome = new Outcome(1, 1, "name", direction, "motivation", new SemanticVariable(uri, "label"));
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(1, 1, "name", outcome);
    List<Integer> outcomeIds = Collections.singletonList(1);
    List<NetworkMetaAnalysis> analyses = Collections.singletonList(networkMetaAnalysis);
    when(networkMetaAnalysisRepository.queryByOutcomes(projectId, outcomeIds)).thenReturn(analyses);

    ResultActions result = mockMvc
        .perform(get("/projects/{projectId}/analyses", projectId)
            .param("outcomeIds", "1"));
    result
        .andExpect(status().isOk())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].analysisType", Matchers.equalTo(AnalysisType.EVIDENCE_SYNTHESIS)));

    verify(networkMetaAnalysisRepository).queryByOutcomes(projectId, outcomeIds);

  }

  @Test
  public void testCreateMetaBenefitRiskAnalysis() throws Exception {
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(1, 1, "title", Collections.emptySet());
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", AnalysisType.BENEFIT_RISK_ANALYSIS_LABEL);
    when(analysisService.createBenefitRiskAnalysis(gert, analysisCommand)).thenReturn(analysis);
    String body = TestUtils.createJson(analysisCommand);
    mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).createBenefitRiskAnalysis(gert, analysisCommand);
  }

  @Test
  public void testCreateNetworkMetaAnalysis() throws Exception {
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(1, 1, "name");
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", AnalysisType.EVIDENCE_SYNTHESIS);
    when(analysisService.createNetworkMetaAnalysis(gert, analysisCommand)).thenReturn(analysis);
    String body = TestUtils.createJson(analysisCommand);
    mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).createNetworkMetaAnalysis(gert, analysisCommand);
  }

  @Test(expected = NestedServletException.class)
  public void testCreateUnknownAnalysisTypeFails() throws Exception {
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", "unknown type");
    String body = TestUtils.createJson(analysisCommand);
    try {
      mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()));
    } catch (Exception e) {
      verify(accountRepository).findAccountByUsername("gert");
      throw e;
    }
  }

  @Test
  public void testGetNMAnalysis() throws Exception {
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(1, projectId, "testName", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
    when(analysisRepository.get(analysis.getId())).thenReturn(analysis);
    ResultActions result = mockMvc.perform(get("/projects/1/analyses/1").principal(user));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$.id", is(analysis.getId())))
        .andExpect(jsonPath("$.analysisType", is(AnalysisType.EVIDENCE_SYNTHESIS)))
        .andExpect(jsonPath("$.excludedArms", hasSize(0)));
    verify(analysisRepository).get(analysis.getId());
  }

  @Test
  public void testUpdateNetworkMetaAnalysis() throws Exception {
    Integer outcomeId = 444;
    Integer direction = 1;
    NetworkMetaAnalysis oldAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "analysis name");
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", direction, "motivation", new SemanticVariable(uri, "label"));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(oldAnalysis.getId(), oldAnalysis.getProjectId(), oldAnalysis.getTitle(), outcome);
    when(analysisRepository.get(analysisId)).thenReturn(oldAnalysis);
    when(analysisService.updateNetworkMetaAnalysis(gert, newAnalysis)).thenReturn(newAnalysis);
    AnalysisUpdateCommand newAnalysisCommand = new AnalysisUpdateCommand(newAnalysis, null);
    String jsonCommand = TestUtils.createJson(newAnalysisCommand);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
        .content(jsonCommand)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testUpdateWithExcludedArms() throws Exception {
    Integer outcomeId = 444;
    Integer direction = 1;
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", direction, "motivation", new SemanticVariable(uri, "label"));
    List<ArmExclusion> excludedArms = Arrays.asList(new ArmExclusion(analysisId, URI.create("-1L")), new ArmExclusion(analysisId, URI.create("-2L")));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "name", excludedArms, Collections.emptyList(), Collections.emptyList(), outcome);
    AnalysisUpdateCommand newAnalysisCommand = new AnalysisUpdateCommand(newAnalysis, null);
    String jsonCommand = TestUtils.createJson(newAnalysisCommand);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
        .content(jsonCommand)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testUpdateWithIncludedInterventions() throws Exception {
    Integer outcomeId = 444;
    Integer direction = 1;
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", direction, "motivation", new SemanticVariable(uri, "label"));
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(1, 1, "adsf");
    List<InterventionInclusion> includedInterventions = Arrays.asList(new InterventionInclusion(analysis.getId(), -1), new InterventionInclusion(analysis.getId(), -2));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "name", Collections.emptyList(), includedInterventions, Collections.emptyList(), outcome);
    AnalysisUpdateCommand newAnalysisCommand = new AnalysisUpdateCommand(newAnalysis, null);
    String jsonCommand = TestUtils.createJson(newAnalysisCommand);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
        .content(jsonCommand)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testUpdateWithIncludedCovariates() throws Exception {
    Integer outcomeId = 444;
    Integer direction = 1;
    Outcome outcome = new Outcome(outcomeId, projectId, "outcome name", direction, "motivation", new SemanticVariable(uri, "label"));
    List<CovariateInclusion> covariateInclusions = Arrays.asList(new CovariateInclusion(analysisId, -1), new CovariateInclusion(analysisId, -2));
    NetworkMetaAnalysis newAnalysis = new NetworkMetaAnalysis(analysisId, projectId, "name", Collections.emptyList(), Collections.emptyList(), covariateInclusions, outcome);
    AnalysisUpdateCommand newAnalysisCommand = new AnalysisUpdateCommand(newAnalysis, null);
    String jsonCommand = TestUtils.createJson(newAnalysisCommand);

    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
        .content(jsonCommand)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisService).updateNetworkMetaAnalysis(gert, newAnalysis);
  }

  @Test
  public void testSetPrimaryModel() throws Exception {
    String modelId = "5";
    mockMvc.perform((post("/projects/{projectId}/analyses/{analysisId}/setPrimaryModel", projectId, analysisId)
        .param("modelId", modelId))
        .principal(user))
        .andExpect(status().isOk());
    verify(projectService).checkOwnership(projectId, user);
    verify(networkMetaAnalysisRepository).setPrimaryModel(analysisId, Integer.parseInt(modelId));
  }


  @Test
  public void testUnsetPrimaryModel() throws Exception {
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}/setPrimaryModel", projectId, analysisId)
        .principal(user))
        .andExpect(status().isOk());
    verify(projectService).checkOwnership(projectId, user);
    verify(networkMetaAnalysisRepository).setPrimaryModel(analysisId, null);
  }

  @Test
  public void testArchiveProject() throws Exception {
    String postBodyStr = "{ \"isArchived\": true }";
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}/setArchivedStatus", projectId, analysisId)
        .content(postBodyStr)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(projectService).checkOwnership(1, user);
    verify(analysisRepository).setArchived(1, true);
  }

  @Test
  public void testUnArchiveProject() throws Exception {
    String postBodyStr = "{ \"isArchived\": false }";
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}/setArchivedStatus", projectId, analysisId)
        .content(postBodyStr)
        .principal(user)
        .contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(status().isOk());
    verify(projectService).checkOwnership(1, user);
    verify(analysisRepository).setArchived(1, false);
  }

  @Test
  public void testGetEvidenceTable() throws Exception, ReadValueException {
    TrialDataStudy study1 = new TrialDataStudy();
    TrialDataStudy study2 = new TrialDataStudy();
    List<TrialDataStudy> studies = Arrays.asList(study1, study2);
    when(analysisService.buildEvidenceTable(projectId, analysisId)).thenReturn(studies);

    mockMvc.perform(get("/projects/{projectId}/analyses/{analysisId}/evidenceTable", projectId, analysisId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
        .andExpect(jsonPath("$", hasSize(2)));
    verify(analysisService).buildEvidenceTable(projectId, analysisId);
  }

}
