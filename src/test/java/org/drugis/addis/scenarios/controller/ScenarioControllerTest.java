package org.drugis.addis.scenarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.scenarios.service.ScenarioService;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

  @Mock
  private ScenarioService scenarioService;

  @Mock
  private ProjectService projectService;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private ScenarioController scenarioController;

  private Principal user;

  @Before
  public void setUp() {
    reset(accountRepository);
    reset(scenarioRepository);
    scenarioService = mock(ScenarioService.class);
    projectService = mock(ProjectService.class);
    scenarioController = new ScenarioController();

    MockitoAnnotations.initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(scenarioController).build();

    user = mock(Principal.class);
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, scenarioRepository);
  }

  @Test
  public void testGet() throws Exception {
    Scenario scenario = new Scenario(1, 1, "Default", "problem");
    when(scenarioRepository.get(scenario.getId())).thenReturn(scenario);
    mockMvc.perform(get("/projects/1/analyses/1/scenarios/" + scenario.getId()).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", is(scenario.getId())));
    verify(scenarioRepository).get(scenario.getId());
  }

  @Test
  public void testQuery() throws Exception {
    Scenario scenario1 = new Scenario(1, 1, "Default", "problem");
    Scenario scenario2 = new Scenario(2, 1, "Default", "problem");
    Integer projectId = 1;
    Integer analysisId = 1;
    Collection<Scenario> scenarios = Arrays.asList(scenario1, scenario2);
    when(scenarioRepository.queryByProjectAndAnalysis(projectId, analysisId)).thenReturn(scenarios);
    mockMvc.perform(get("/projects/" + projectId + "/analyses/" + analysisId + "/scenarios").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(scenario1.getId())))
            .andExpect(jsonPath("$[1].id", is(scenario2.getId())));
    verify(scenarioRepository).queryByProjectAndAnalysis(projectId, analysisId);
  }

  @Test
  public void testUpdate() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    Scenario scenario = new Scenario(1, 1, "Default", "{\"key\":\"value\"}");
    String content = TestUtils.createJson(scenario);
    System.out.println(content);
    when(scenarioRepository.update(scenario.getId(), scenario.getTitle(), scenario.getState())).thenReturn(scenario);
    mockMvc.perform(post("/projects/" + projectId + "/analyses/" + analysisId + "/scenarios/" + scenario.getId())
            .content(content)
            .principal(user)
            .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());

    verify(scenarioService).checkCoordinates(projectId, analysisId, scenario);
    verify(projectService).checkOwnership(projectId, user);
    verify(scenarioRepository).update(scenario.getId(), scenario.getTitle(), scenario.getState());
  }

  @Test
  public void testCreate() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    Scenario scenario = new Scenario(1, "Default", "{\"key\":\"value\"}");
    String content = TestUtils.createJson(scenario);
    when(scenarioRepository.create(analysisId, scenario.getTitle(), scenario.getState())).thenReturn(scenario);
    mockMvc.perform(post("/projects/" + projectId + "/analyses/" + analysisId + "/scenarios/")
      .content(content).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$.title", is(scenario.getTitle())));

    verify(scenarioService).checkCoordinates(projectId, analysisId, scenario);
    verify(projectService).checkOwnership(projectId, user);
    verify(scenarioRepository).create(analysisId, scenario.getTitle(), scenario.getState());
  }

  @Test
  public void testComplex() throws Exception {
    Integer projectId = 1;
    Integer analysisId = 1;
    String content = "{\"title\":\"Scenario z32\",\"state\":{\"problem\":{\"title\":\"foo\",\"alternatives\":{\"plac\":{\"title\":\"plac\"},\"az\":{\"title\":\"az\"}},\"criteria\":{\"nonserious-ads\":{\"title\":\"non-serious ads\",\"scale\":[0,1],\"pvf\":{\"range\":[0.012,0.136],\"type\":\"linear\",\"direction\":\"decreasing\"},\"id\":\"nonserious-ads\",\"w\":\"w_1\"},\"serious-ads\":{\"title\":\"Serious ads\",\"scale\":[0,1],\"pvf\":{\"range\":[0.001,0.037],\"type\":\"linear\",\"direction\":\"decreasing\"},\"id\":\"serious-ads\",\"w\":\"w_2\"}},\"performanceTable\":[{\"alternative\":\"az\",\"criterion\":\"serious-ads\",\"performance\":{\"parameters\":{\"alpha\":2,\"beta\":137},\"type\":\"dbeta\"}},{\"alternative\":\"plac\",\"criterion\":\"serious-ads\",\"performance\":{\"parameters\":{\"alpha\":1,\"beta\":139},\"type\":\"dbeta\"}},{\"alternative\":\"plac\",\"criterion\":\"nonserious-ads\",\"performance\":{\"parameters\":{\"alpha\":4,\"beta\":136},\"type\":\"dbeta\"}},{\"alternative\":\"az\",\"criterion\":\"nonserious-ads\",\"performance\":{\"parameters\":{\"alpha\":10,\"beta\":129},\"type\":\"dbeta\"}}],\"method\":\"scales\"}}}";
    ObjectMapper mapper = new ObjectMapper();
    Scenario scenario = mapper.readValue(content, Scenario.class);
    when(scenarioRepository.create(analysisId, scenario.getTitle(), scenario.getState())).thenReturn(scenario);
    //NB: controller sets workspace/analysisID in scenario
    scenario.setWorkspace(analysisId);
    mockMvc.perform(post("/projects/" + projectId + "/analyses/" + analysisId + "/scenarios/")
      .content(content).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(status().isCreated());
    verify(scenarioService).checkCoordinates(projectId, analysisId, scenario);
    verify(projectService).checkOwnership(projectId, user);
    verify(scenarioRepository).create(analysisId, "Scenario z32", scenario.getState());
  }

}
