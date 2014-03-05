package org.drugis.addis.outcomes;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
  private ProjectRepository projectRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account john = new Account(1, "a", "john", "lennon"),
    paul = new Account(2, "a", "paul", "mc cartney"),
    gert = new Account(3, "gert", "Gert", "van Valkenhoef");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(projectRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, projectRepository);
  }

  @Test
  public void testQueryOutcomes() throws Exception {
    Outcome outcome = new Outcome(1, "name", "motivation", "uri");
    Integer projectId = 1;
    List<Outcome> outcomes = Arrays.asList(outcome);
    Project project = mock(Project.class);
    when(project.getOutcomes()).thenReturn(outcomes);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);

    mockMvc.perform(get("/projects/1/outcomes").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id", is(outcome.getId())));

    verify(projectRepository).getProjectById(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testUnauthorisedAccessFails() throws Exception {
    when(accountRepository.findAccountByUsername("gert")).thenReturn(null);
    mockMvc.perform(get("/projects/1/outcomes").principal(user))
      .andExpect(redirectedUrl("/error/403"));
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testGetOutcome() throws Exception {
    Outcome outcome = new Outcome(1, "name", "motivation", "uri");
    Integer projectId = 1;
    when(projectRepository.getProjectOutcome(projectId, outcome.getId())).thenReturn(outcome);
    mockMvc.perform(get("/projects/1/outcomes/1").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.id", is(outcome.getId())));
    verify(accountRepository).findAccountByUsername("gert");
    verify(projectRepository).getProjectOutcome(projectId, outcome.getId());
  }

}
