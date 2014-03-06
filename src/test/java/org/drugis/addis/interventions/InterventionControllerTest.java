package org.drugis.addis.interventions;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.util.WebConstants;
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
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 3/6/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class InterventionControllerTest {

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

  @Test
  public void testQueryInterventions() throws Exception {
    Intervention intervention = new Intervention(1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Integer projectId = 1;
    Set<Intervention> interventions = new HashSet<>(Arrays.asList(intervention));
    Project project = mock(Project.class);
    when(project.getInterventions()).thenReturn(interventions);
    when(projectRepository.getProjectById(projectId)).thenReturn(project);

    mockMvc.perform(get("/projects/1/interventions").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(intervention.getId())));

    verify(projectRepository).getProjectById(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

}
