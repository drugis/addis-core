package org.drugis.addis.analysis;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.CriteriaRepository;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
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

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  CriteriaRepository criteriaRepository;


  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account john = new Account(1, "a", "john", "lennon"),
    paul = new Account(2, "a", "paul", "mc cartney"),
    gert = new Account(3, "gert", "Gert", "van Valkenhoef");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(analysisRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, analysisRepository);
  }

  @Test
  public void testQueryAnalyses() throws Exception {
    Analysis analysis = new Analysis(1, 1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST);
    Integer projectId = 1;
    List<Analysis> analyses = Arrays.asList(analysis);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    mockMvc.perform(get("/projects/1/analyses").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id", is(analysis.getId())))
      .andExpect(jsonPath("$[0].analysisType", is(AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL)));

    verify(analysisRepository).query(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testUnauthorisedAccessFails() throws Exception {
    when(accountRepository.findAccountByUsername("gert")).thenReturn(null);
    mockMvc.perform(get("/projects/1/analyses").principal(user))
      .andExpect(redirectedUrl("/error/403"));
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testCreateAnalysis() throws Exception {
    Analysis analysis = new Analysis(1, 1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST);
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
    when(analysisRepository.create(gert, analysisCommand)).thenReturn(analysis);
    String body = TestUtils.createJson(analysisCommand);
    mockMvc.perform(post("/projects/1/analyses").content(body).principal(user).contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).create(gert, analysisCommand);
  }

  @Test
  public void testGetAnalysis() throws Exception {
    Analysis analysis = new Analysis(1, 1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST);
    Integer projectId = 1;
    when(analysisRepository.get(projectId, analysis.getId())).thenReturn(analysis);
    mockMvc.perform(get("/projects/1/analyses/1").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.id", is(analysis.getId())));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).get(projectId, analysis.getId());
  }


  @Test
  public void testUpdateAnalysis() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    List<Integer> selectedOutcomeIds = Arrays.asList(1, 2, 3);
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, selectedOutcomeIds);
    List<Outcome> selectedOutcomes = Arrays.asList(
      new Outcome(1, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
      new Outcome(2, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
      new Outcome(3, projectId, "name", "motivation", new SemanticOutcome("uri", "label"))
    );
    Analysis analysis = new Analysis(1, analysisCommand.getProjectId(), analysisCommand.getName(), AnalysisType.getByLabel(analysisCommand.getType()), selectedOutcomes);
    String body = TestUtils.createJson(analysis);
    System.out.println(body);
    when(analysisRepository.update(gert, analysisId, analysis)).thenReturn(analysis);
    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId, analysisId)
      .content(body)
      .principal(user)
      .contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.selectedOutcomes", hasSize(3)));
    verify(accountRepository).findAccountByUsername("gert");
    verify(analysisRepository).update(gert, analysisId, analysis);
  }

//  @Test
//  public void testUpdateAnalysi2() throws Exception {
//    Integer projectId = 1;
//    Integer analysisId = 1;
//    List<Integer> selectedOutcomeIds = Arrays.asList();
//    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, selectedOutcomeIds);
//    String body = "{\"id\":1,\"projectId\":1,\"name\":\"sadf\",\"analysisType\":\"Single-study Benefit-Risk\",\"study\":null,\"selectedOutcomeIds\":[]}";
//    System.out.println(body);
//    System.out.println(TestUtils.createJson(analysisCommand));
//
////    String body = "{\"id\":1,\"projectId\":1,\"name\":\"sadf\",\"analysisType\":\"Single-study Benefit-Risk\",\"study\":null,\"selectedOutcomes\":[\"2\",\"3\"]}";
//    List<Outcome> selectedOutcomes = Arrays.asList(
//      new Outcome(1, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
//      new Outcome(2, projectId, "name", "motivation", new SemanticOutcome("uri", "label")),
//      new Outcome(3, projectId, "name", "motivation", new SemanticOutcome("uri", "label"))
//    );
//    Analysis analysis = new Analysis(1, analysisCommand.getProjectId(), analysisCommand.getName(), AnalysisType.getByLabel(analysisCommand.getType()), selectedOutcomes);
//    when(analysisRepository.update(gert, analysisId, analysisCommand)).thenReturn(analysis);
//    mockMvc.perform(post("/projects/{projectId}/analyses/{analysisId}", projectId , analysisId)
//      .content(body)
//      .principal(user)
//      .contentType(WebConstants.APPLICATION_JSON_UTF8))
//      .andExpect(status().isOk())
//      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
//      .andExpect(jsonPath("$.selectedOutcomes", hasSize(3)));
//    verify(accountRepository).findAccountByUsername("gert");
//    verify(analysisRepository).update(gert, analysisId, analysisCommand);
//  }


}
