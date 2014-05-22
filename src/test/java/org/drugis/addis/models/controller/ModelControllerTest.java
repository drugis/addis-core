package org.drugis.addis.models.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.InterventionCommand;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ModelControllerTest {
  private MockMvc mockMvc;

  @Mock
  private AnalysisService analysisService;

  @Mock
  private ProjectService projectService;

  @Mock
  private ModelService modelService;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private ModelController modelController;

  private Principal user;

  @Before
  public void setUp() {
    analysisService = mock(AnalysisService.class);
    projectService = mock(ProjectService.class);
    modelService = mock(ModelService.class);
    modelController = new ModelController();

    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(modelController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(analysisService, projectService, modelService );
  }

  @Test
  public void testCreate() throws Exception {
    mockMvc.perform(post("/projects/1/analyses/5/model").principal(user).contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.id", notNullValue()));
  }
}