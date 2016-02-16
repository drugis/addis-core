package org.drugis.addis.outcomes;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
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
public class OutcomeControllerTest {

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(outcomeRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, outcomeRepository);
  }

  @Test
  public void testQueryOutcomes() throws Exception {
    Outcome outcome = new Outcome(1, "name", "motivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    Integer projectId = 1;
    List<Outcome> outcomes = Arrays.asList(outcome);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);

    mockMvc.perform(get("/projects/1/outcomes").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id", is(outcome.getId())));

    verify(outcomeRepository).query(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testUnauthorisedAccessFails() throws Exception {
    when(accountRepository.findAccountByUsername("gert")).thenReturn(null);
    mockMvc.perform(get("/projects/1/outcomes").principal(user))
            .andExpect(status().isForbidden());
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testGetOutcome() throws Exception {
    Outcome outcome = new Outcome(1, 1, "name", "motivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    Integer projectId = 1;
    when(outcomeRepository.get(projectId, outcome.getId())).thenReturn(outcome);
    mockMvc.perform(get("/projects/1/outcomes/1").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$.id", is(outcome.getId())));
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeRepository).get(projectId, outcome.getId());
  }

  @Test
  public void testCreateOutcome() throws Exception {
    Outcome outcome = new Outcome(1, 1, "name", "motivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "name", "motivation", new SemanticOutcome("http://semantic.com", "labelnew"));
    when(outcomeRepository.create(gert, outcomeCommand)).thenReturn(outcome);
    String body = TestUtils.createJson(outcomeCommand);
    mockMvc.perform(post("/projects/1/outcomes").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeRepository).create(gert, outcomeCommand);
  }

  @Test
  public void testHandleBlankMotivation() throws Exception {
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "name", null, new SemanticOutcome("http://semantic.com", "labelnew"));
    Outcome outcome = new Outcome(1, 1, "name", "", new SemanticOutcome("http://semantic.com", "labelnew"));
    when(outcomeRepository.create(gert, outcomeCommand)).thenReturn(outcome);
    String body = "{\"name\":\"name\",\"semanticOutcome\":{\"uri\":\"http://semantic.com\",\"label\":\"labelnew\"},\"projectId\":1}";
    mockMvc.perform(post("/projects/1/outcomes").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeRepository).create(gert, outcomeCommand);
  }

}
