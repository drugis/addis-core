package org.drugis.addis.projects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by daan on 2/6/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {TestConfig.class})
@WebAppConfiguration
public class ProjectsControllerTest {
  public static final MediaType APPLICATION_JSON_UTF8 =
    new MediaType(
      MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      Charset.forName("utf8"));

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account john = new Account(1, "a", "john", "lennon"),
          paul = new Account(2, "a", "paul", "mc cartney");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(projectRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    Account gert = new Account(1, "gert", "Gert", "van Valkenhoef");
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
      .andExpect(content().contentType(APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", hasSize(0)));

    verify(projectRepository).query();
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testQueryProjects() throws Exception {
    Project project = new Project(1 ,john, "name", "desc", "ns1");
    Project project2 = new Project(2 ,paul, "otherName", "other description", "ns2");
    ArrayList projects = new ArrayList();
    projects.add(project);
    projects.add(project2);
    when(projectRepository.query()).thenReturn(projects);

    mockMvc.perform(get("/projects").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", hasSize(projects.size())))
      .andExpect(jsonPath("$[0].id", is(project.getId())))
      .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())))
      .andExpect(jsonPath("$[0].name", is(project.getName())))
      .andExpect(jsonPath("$[0].description", is(project.getDescription())))
      .andExpect(jsonPath("$[0].namespace", is(project.getNamespace())));

    verify(projectRepository).query();
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testQueryProjectsWithQueryString() throws Exception {
    Project project = new Project(2, paul, "test2", "desc", "ns1");
    ArrayList projects = new ArrayList();
    projects.add(project);
    when(projectRepository.queryByOwnerId(paul.getId())).thenReturn(projects);

    mockMvc.perform(get("/projects?owner=2").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(projects.size())))
            .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())));

    verify(projectRepository).queryByOwnerId(paul.getId());
    verify(accountRepository).findAccountByUsername("gert");
  }

}
