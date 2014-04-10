package org.drugis.addis.scenarios.controller;

import org.drugis.addis.analyses.State;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
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
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 3-4-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ScenarioControllerTest {

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ScenarioRepository scenarioRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  @Before
  public void setUp() {
    reset(accountRepository);
    reset(scenarioRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, scenarioRepository);
  }

  @Test
  public void testGet() throws Exception {
    Scenario scenario = new Scenario(1, 1, "Default", new State("problem"));
    when(scenarioRepository.get(scenario.getId())).thenReturn(scenario);
    mockMvc.perform(get("/projects/1/analyses/1/scenarios/" + scenario.getId()).principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.id", is(scenario.getId())));
    verify(scenarioRepository).get(scenario.getId());
  }

  @Test
  public void testQuery() throws Exception {
    Scenario scenario1 = new Scenario(1, 1, "Default", new State("problem"));
    Scenario scenario2 = new Scenario(2, 1, "Default", new State("problem"));
    Integer projectId = 1;
    Integer analysisId = 1;
    Collection<Scenario> scenarios = Arrays.asList(scenario1, scenario2);
    when(scenarioRepository.query(projectId, analysisId)).thenReturn(scenarios);
    mockMvc.perform(get("/projects/" + projectId + "/analyses/" + analysisId + "/scenarios").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", hasSize(2)))
      .andExpect(jsonPath("$[0].id", is(scenario1.getId())))
      .andExpect(jsonPath("$[1].id", is(scenario2.getId())));
    verify(scenarioRepository).query(projectId, analysisId);
  }

}
