package org.drugis.addis.models.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.models.DetailsCommand;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.ModelCommand;
import org.drugis.addis.models.ModelTypeCommand;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.projects.service.ProjectService;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

  @InjectMocks
  private AbstractAddisCoreController abstractAddisCoreController;

  private Principal user;

  @Before
  public void setUp() {
    abstractAddisCoreController = new AbstractAddisCoreController();
    modelController = new ModelController();

    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(abstractAddisCoreController, modelController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(analysisService, projectService, modelService);
  }

  @Test
  public void testCreate() throws Exception, InvalidModelTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = "network";



    Model model = new Model(1, 2, modelTitle, linearModel, "{'type': 'network'}");
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", new DetailsCommand());

    //  "{\"linearModel\":\"random\",\"modelType\":{\"type\":\"pairwise\",\"details\":{\"from\":\"Paroxetine\",\"to\":\"Fluoxetine\"}},\"title\":\"asdf\"}";
    String body = TestUtils.createJson(new ModelCommand(modelTitle, linearModel, modelTypeCommand));

    when(modelService.createModel(projectId, analysisId, modelTitle, linearModel, modelType, null, null)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
              .content(body)
              .principal(user)
              .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(projectId, analysisId, modelTitle, linearModel, modelType, null, null);
  }

  @Test
  public void testCreatePairwise() throws Exception, InvalidModelTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = "pairwise";

    Model model = new Model(1, 2, modelTitle, linearModel, "{'type': 'pairwise', 'details':{ 'from': 't1', 'to': 't2'}}");
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(modelType, new DetailsCommand("t1", "t2"));

    String body = TestUtils.createJson(new ModelCommand(modelTitle, linearModel, modelTypeCommand));

    when(modelService.createModel(projectId, analysisId, modelTitle, linearModel, modelType, "t1", "t2")).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(projectId, analysisId, modelTitle, linearModel, modelType, "t1", "t2");
  }

  @Test
  public void testGet() throws Exception {
    Integer analysisId = 55;
    Integer modelId = 12;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = "{'type': 'network'}";
    Model model = new Model(modelId, analysisId, modelTitle, linearModel, modelType);

    when(modelService.getModel(analysisId, model.getId())).thenReturn(model);
    mockMvc.perform(get("/projects/45/analyses/55/models/12").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(modelId)))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));


    verify(modelService).getModel(analysisId, modelId);
  }

  @Test
  public void testQueryWithModelResult() throws Exception {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = "{'type': 'network'}";
    Model model = new Model(-1, analysisId, modelTitle, linearModel, modelType);
    List<Model> models = Arrays.asList(model);
    when(modelService.query(analysisId)).thenReturn(models);
    mockMvc.perform(get("/projects/45/analyses/55/models").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].id", notNullValue()));
    verify(modelService).query(analysisId);

  }

  @Test
  public void testQueryWithNoModelResult() throws Exception {
    Integer analysisId = 55;
    when(modelService.query(analysisId)).thenReturn(Collections.EMPTY_LIST);
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    verify(modelService).query(analysisId);
  }
}