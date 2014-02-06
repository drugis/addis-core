package org.drugis.addis.projects;

import static org.hamcrest.Matchers.hasSize;
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
}
