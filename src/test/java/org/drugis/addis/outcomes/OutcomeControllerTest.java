package org.drugis.addis.outcomes;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.outcomes.controller.command.EditOutcomeCommand;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.OutcomeService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

  @Inject
  private OutcomeService outcomeService;

  @Inject
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
    when(accountRepository.getAccount(user)).thenReturn(gert);
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, outcomeRepository);
  }

  @Test
  public void testQueryOutcomes() throws Exception {
    Outcome outcome = new Outcome(1, 1, "name", "motivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
    Integer projectId = 1;
    List<Outcome> outcomes = Collections.singletonList(outcome);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);

    mockMvc.perform(get("/projects/1/outcomes"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id", is(outcome.getId())));

    verify(outcomeRepository).query(projectId);
  }

  @Test
  public void testGetOutcome() throws Exception {
    Outcome outcome = new Outcome(1, 1, "name", 1, "motivation", new SemanticVariable(URI.create("http://semantic.com"), "labelnew"));
    Integer projectId = 1;
    when(outcomeRepository.get(projectId, outcome.getId())).thenReturn(outcome);
    mockMvc.perform(get("/projects/1/outcomes/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id", is(outcome.getId())));
    verify(outcomeRepository).get(projectId, outcome.getId());
  }

  @Test
  public void testCreateOutcome() throws Exception {
    SemanticVariable semanticOutcome = new SemanticVariable(URI.create("http://semantic.com"), "labelnew");
    Outcome outcome = new Outcome(1, 1, "name", 1, "motivation", semanticOutcome);
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "name", 1, "motivation", semanticOutcome);
    when(outcomeRepository.create(gert, 1, "name", 1, "motivation", semanticOutcome)).thenReturn(outcome);
    String body = TestUtils.createJson(outcomeCommand);
    mockMvc.perform(post("/projects/1/outcomes")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeRepository).create(gert, 1, "name", 1,"motivation", semanticOutcome);
  }

  @Test
  public void testHandleBlankMotivation() throws Exception {
    SemanticVariable semanticOutcome = new SemanticVariable(URI.create("http://semantic.com"), "labelnew");
    Outcome outcome = new Outcome(1, 1, "name", "", semanticOutcome);
    when(outcomeRepository.create(gert, 1, "name", 1, "", semanticOutcome)).thenReturn(outcome);
    OutcomeCommand outcomeCommand = new OutcomeCommand(1, "name", 1, "", semanticOutcome);
    String body = TestUtils.createJson(outcomeCommand);
    mockMvc.perform(post("/projects/1/outcomes")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8_VALUE))
      .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeRepository).create(gert, 1, "name", 1, "", semanticOutcome);
  }

  @Test
  public void editOutcome() throws Exception {
    EditOutcomeCommand editOutcomeCommand = new EditOutcomeCommand("new name", "new motivation", -1);
    Integer outcomeId = 1;
    Integer projectId = 2;
    Outcome outcome = new Outcome(outcomeId, projectId, editOutcomeCommand.getName(), editOutcomeCommand.getMotivation(), new SemanticVariable(URI.create("uri"), "label"));
    when(outcomeService.updateOutcome(projectId, outcomeId, editOutcomeCommand.getName(), editOutcomeCommand.getMotivation(), editOutcomeCommand.getDirection())).thenReturn(outcome);
    String body = TestUtils.createJson(editOutcomeCommand);
    mockMvc.perform(post("/projects/2/outcomes/1").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    verify(accountRepository).getAccount(user);
    verify(outcomeService).updateOutcome(projectId, outcomeId, editOutcomeCommand.getName(), editOutcomeCommand.getMotivation(), editOutcomeCommand.getDirection());
  }

  @Test
  public void deleteOutcome() throws Exception {
    mockMvc.perform(delete("/projects/2/outcomes/1").principal(user))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(outcomeService).delete(2, 1);
  }

}
