package org.drugis.addis.analysiss;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
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
    Analysis analysis = new Analysis(1, 1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK);
    Integer projectId = 1;
    List<Analysis> analyses = Arrays.asList(analysis);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    mockMvc.perform(get("/projects/1/analyses").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(analysis.getId())));

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
    Analysis analysis = new Analysis(1, 1, "name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK);
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

}
