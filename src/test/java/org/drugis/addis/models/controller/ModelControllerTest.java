package org.drugis.addis.models.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.models.*;
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
  public void testCreateNetwork() throws Exception, InvalidModelTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder()
            .id(1)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
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

    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreateModelWithFixedOutcomeScale() throws Exception, InvalidModelTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Double outcomeScale = 2.2;

    Model model = new Model.ModelBuilder()
            .id(1)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated());

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreatePairwise() throws Exception, InvalidModelTypeException {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = Model.PAIRWISE_MODEL_TYPE;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder()
            .id(1)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(modelType)
            .from(new Model.DetailNode(-1, "t1"))
            .to(new Model.DetailNode(-2, "t2"))
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(modelType, new DetailsCommand(new NodeCommand(-1, "t1"), new NodeCommand(-2, "t2")));

    CreateModelCommand createModelCommand = new CreateModelCommand(modelTitle, linearModel, modelTypeCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
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

    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testGet() throws Exception, InvalidModelTypeException {
    Integer analysisId = 55;
    Integer modelId = 12;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = Model.NETWORK_MODEL_TYPE;

    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;

    Model model = new Model.ModelBuilder()
            .id(modelId)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(modelType)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .build();

    when(modelService.getModel(analysisId, model.getId())).thenReturn(model);
    mockMvc.perform(get("/projects/45/analyses/55/models/12").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(modelId)))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));

    verify(modelService).getModel(analysisId, modelId);
  }

  @Test
  public void testQueryWithModelResult() throws Exception, InvalidModelTypeException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = Model.NETWORK_MODEL_TYPE;

    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;

    Model model = new Model.ModelBuilder()
            .id(-1)
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(modelType)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .build();
    List<Model> models = Collections.singletonList(model);
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
    when(modelService.query(analysisId)).thenReturn(Collections.<Model>emptyList());
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    verify(modelService).query(analysisId);
  }
}