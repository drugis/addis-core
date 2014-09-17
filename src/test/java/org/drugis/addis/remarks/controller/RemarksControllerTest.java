package org.drugis.addis.remarks.controller;


import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.scenarios.service.ScenarioService;
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

import javax.inject.Inject;

import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by connor on 17-9-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class RemarksControllerTest {
  private MockMvc mockMvc;

  @Mock
  private AnalysisService analysisService;

  @Mock
  private ProjectService projectService;

  @Inject
  RemarksRepository remarksRepository;
  private Principal user;

  @InjectMocks
  private RemarksController remarksController;

  @Before
  public void setUp() {
    reset(remarksRepository);
    analysisService = mock(AnalysisService.class);

    projectService = mock(ProjectService.class);
    remarksController = new RemarksController();
    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(remarksController).build();
    user = mock(Principal.class);

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(remarksRepository);
  }

  @Test
  public void testGetRemarks() throws Exception {
    Integer remarksId = 11;
    String remarksStr = "{" +
            "\"HAM-D responders\":\"test content 1\"" +
            "}";
    Integer analysisId = 2;
    Remarks remarks = new Remarks(remarksId, analysisId, remarksStr);
    when(remarksRepository.find(analysisId)).thenReturn(remarks);
    mockMvc.perform(get("/projects/22/analyses/2/remarks").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(remarksId)))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));
    verify(remarksRepository).find(analysisId);
  }

  @Test
  public void testSaveNewRemark() throws Exception {
    Integer analysisId = 2;
    Integer projectId = 22;
    String remarksStr = "{" +
            "\"HAM-D responders\":\"test content 1\"" +
            "}";
    Remarks remarks = new Remarks(null, analysisId, remarksStr);
    String content = TestUtils.createJson(remarks);
    Scenario scenario = mock(Scenario.class);
    when(scenario.getWorkspace()).thenReturn(analysisId);
    when(remarksRepository.create(analysisId, remarksStr)).thenReturn(remarks);
    mockMvc.perform(post("/projects/22/analyses/2/remarks")
      .principal(user)
      .content(content)
      .contentType(WebConstants.APPLICATION_JSON_UTF8))
    .andExpect(status().isCreated())
    .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
    .andExpect(jsonPath("$.analysisId", is(analysisId)));
    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(remarksRepository).create(analysisId, remarksStr);
  }

  @Test
  public void testUpdateRemark() throws Exception {
    Integer analysisId = 2;
    Integer projectId = 22;
    String remarksStr = "{" +
            "\"HAM-D responders\":\"test content 1\"" +
            "}";
    Remarks remarks = new Remarks(-1, analysisId, remarksStr);
    String content = TestUtils.createJson(remarks);
    Scenario scenario = mock(Scenario.class);
    when(scenario.getWorkspace()).thenReturn(analysisId);

    when(remarksRepository.update(remarks)).thenReturn(remarks);
    mockMvc.perform(post("/projects/22/analyses/2/remarks")
            .principal(user)
            .content(content)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));
    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(remarksRepository).update(remarks);
  }

}
