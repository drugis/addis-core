package org.drugis.addis.projects;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
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
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 2/6/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ProjectsControllerTest {

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
  public void testQueryEmptyProjects() throws Exception {
    when(projectRepository.query()).thenReturn(Collections.<Project>emptyList());

    mockMvc.perform(get("/projects").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(0)));

    verify(projectRepository).query();
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testQueryProjects() throws Exception {
    Project project = new Project(1, john, "name", "desc", 1, new ArrayList<Outcome>());
    Project project2 = new Project(2, paul, "otherName", "other description", 2, new ArrayList<Outcome>());
    ArrayList projects = new ArrayList();
    projects.add(project);
    projects.add(project2);
    when(projectRepository.query()).thenReturn(projects);

    mockMvc.perform(get("/projects").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(projects.size())))
            .andExpect(jsonPath("$[0].id", is(project.getId())))
            .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())))
            .andExpect(jsonPath("$[0].name", is(project.getName())))
            .andExpect(jsonPath("$[0].description", is(project.getDescription())))
            .andExpect(jsonPath("$[0].trialverseId", is(project.getTrialverseId())));

    verify(projectRepository).query();
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testQueryProjectsWithQueryString() throws Exception {
    Project project = new Project(2, paul, "test2", "desc", 1, new ArrayList<Outcome>());
    ArrayList projects = new ArrayList();
    projects.add(project);
    when(projectRepository.queryByOwnerId(paul.getId())).thenReturn(projects);

    mockMvc.perform(get("/projects?owner=2").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(projects.size())))
            .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())));

    verify(projectRepository).queryByOwnerId(paul.getId());
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testCreateProject() throws Exception {
    Project project = new Project(1, gert, "testname", "testdescription", 1, new ArrayList<Outcome>());
    String jsonContent = TestUtils.createJson(project);
    when(projectRepository.create(project.getOwner(), project.getName(), project.getDescription(), project.getTrialverseId())).thenReturn(project);
    mockMvc.perform(post("/projects").principal(user).content(jsonContent).contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.name", is("testname")));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectRepository).create(project.getOwner(), project.getName(), project.getDescription(), project.getTrialverseId());
  }

  @Test
  public void testGetSingleProject() throws Exception {
    Project project = new Project(1, john, "name", "desc", 1, new ArrayList<Outcome>());
    when(projectRepository.getProjectById(project.getId())).thenReturn(project);
    mockMvc.perform(get("/projects/" + project.getId()).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(project.getId())));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectRepository).getProjectById(project.getId());
  }

  @Test
  public void testGetNonexistentProject() throws Exception {
    when(projectRepository.getProjectById(1)).thenThrow(new ResourceDoesNotExistException());
    mockMvc.perform(get("/projects/1").principal(user))
            .andExpect(redirectedUrl("/error/404"));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectRepository).getProjectById(1);
  }

  @Test
  public void testUpdate() throws Exception {
    Project project = new Project(1, gert, "name", "desc", 1, new ArrayList<Outcome>());
    project.addOutcome(new Outcome(1, "name", "motivation", "semantics"));
    String jsonContent = TestUtils.createJson(project);
    when(projectRepository.getProjectById(1)).thenReturn(project);
    when(projectRepository.update(project)).thenReturn(project);

    mockMvc.perform(post("/projects/1").principal(user).content(jsonContent).contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(project.getId())));
    verify(projectRepository).getProjectById(1);
    verify(projectRepository).update(project);
  }

  @Test
  public void testUpdateNotOwnedProjectFails() throws Exception {
    Project project = new Project(1, paul, "name", "desc", 1, new ArrayList<Outcome>());
    project.addOutcome(new Outcome(1, "name", "motivation", "semantics"));
    String jsonContent = TestUtils.createJson(project);
    when(projectRepository.getProjectById(1)).thenReturn(project);
    when(projectRepository.update(project)).thenReturn(project);

    mockMvc.perform(post("/projects/1").principal(user).content(jsonContent).contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(redirectedUrl("/error/403"));
    verify(projectRepository).getProjectById(1);

  }

}

