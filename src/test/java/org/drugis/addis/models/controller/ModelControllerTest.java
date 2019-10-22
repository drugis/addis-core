package org.drugis.addis.models.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.models.*;
import org.drugis.addis.models.controller.command.*;
import org.drugis.addis.models.repository.FunnelPlotRepository;
import org.drugis.addis.models.repository.ModelBaselineRepository;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.NormalBaselineDistribution;
import org.drugis.addis.projects.service.ProjectService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

  @Mock
  private ModelRepository modelRepository;

  @Mock
  private ModelBaselineRepository modelBaselineRepository;

  @Mock
  private FunnelPlotRepository funnelPlotRepository;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @InjectMocks
  private ModelController modelController;

  @InjectMocks
  private AbstractAddisCoreController abstractAddisCoreController;

  private Principal user;
  private Model.ModelBuilder modelBuilder;

  @Before
  public void setUp() {
    abstractAddisCoreController = new AbstractAddisCoreController();
    modelController = new ModelController();

    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(abstractAddisCoreController, modelController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");


    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;

    modelBuilder = new Model.ModelBuilder(analysisId, modelTitle)
            .id(1)
            .link(Model.LINK_IDENTITY)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor);

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(analysisService, projectService, modelService, modelBaselineRepository);
  }

  @Test
  public void testCreateFixedEffectNetwork() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder()
            .setTitle(modelTitle)
            .setLinearModel(linearModel)
            .setModelType(modelTypeCommand)
            .setHeterogeneityPriorCommand(null)
            .setBurnInIterations(burnInIterations)
            .setInferenceIterations(inferenceIterations)
            .setThinningFactor(thinningFactor)
            .setLikelihood(likelihood)
            .setLink(link)
            .build();
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);

  }

  @Test
  public void testCreateNetworkWithStdDevHetPrior() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_RANDOM;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double lower = 0.4;
    Double upper = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE)
            .lower(lower)
            .upper(upper)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);

    HeterogeneityPriorCommand heterogeneityPriorCommand = new StdDevHeterogeneityPriorCommand(new StdDevValuesCommand(lower, upper));
    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreateNetworkWithVarianceHetPrior() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double mean = 0.4;
    Double stdDev = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE)
            .mean(mean)
            .stdDev(stdDev)
            .build();

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = new VarianceHeterogeneityPriorCommand(new VarianceValuesCommand(mean, stdDev));

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    String body = TestUtils.createJson(createModelCommand);


    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);

  }

  @Test
  public void testCreateNetworkWithPrecisionHetPrior() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Double rate = 0.4;
    Double shape = 1.4;
    Model model = modelBuilder
            .heterogeneityPriorType(Model.PRECISION_HETEROGENEITY_PRIOR_TYPE)
            .rate(rate)
            .shape(shape)
            .build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = new PrecisionHeterogeneityPriorCommand(new PrecisionValuesCommand(rate, shape));
    CreateModelCommand modelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    String body = TestUtils.createJson(modelCommand);


    when(modelService.createModel(analysisId, modelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, modelCommand);

  }

  @Test
  public void testCreateModelWithFixedOutcomeScale() throws Exception {
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

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).setOutcomeScale(outcomeScale).build();
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);
    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreatePairwise() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(Model.PAIRWISE_MODEL_TYPE, new DetailsCommand(new NodeCommand(-1, "t1"), new NodeCommand(-2, "t2")));
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testCreateMetaRegressionModel() throws Exception {
    Integer projectId = 45;
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = modelBuilder.build();
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(Model.REGRESSION_MODEL_TYPE);

    JSONObject regressor = new JSONObject();
    regressor.put("a", "b");
    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder()
            .setTitle(modelTitle)
            .setLinearModel(linearModel)
            .setModelType(modelTypeCommand)
            .setHeterogeneityPriorCommand(null)
            .setBurnInIterations(burnInIterations)
            .setInferenceIterations(inferenceIterations)
            .setThinningFactor(thinningFactor)
            .setLikelihood(likelihood)
            .setLink(link)
            .setRegressor(regressor)
            .build();
    String body = TestUtils.createJson(createModelCommand);

    when(modelService.createModel(analysisId, createModelCommand)).thenReturn(model);
    mockMvc.perform(post("/projects/45/analyses/55/models")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.analysisId", notNullValue()));

    verify(analysisService).checkCoordinates(projectId, analysisId);
    verify(projectService).checkOwnership(projectId, user);

    verify(modelService).createModel(analysisId, createModelCommand);
  }

  @Test
  public void testGet() throws Exception {
    Integer analysisId = 55;
    Model model = modelBuilder.build();
    when(modelService.get(model.getId())).thenReturn(model);

    mockMvc.perform(get("/projects/45/analyses/55/models/1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(model.getId())))
            .andExpect(jsonPath("$.analysisId", is(analysisId)));

    verify(modelService).get(model.getId());
  }

  @Test
  public void testQueryWithModelResult() throws Exception {
    Integer analysisId = 55;
    Model model = modelBuilder.build();
    List<Model> models = Collections.singletonList(model);
    when(modelService.query(analysisId)).thenReturn(models);

    mockMvc.perform(get("/projects/45/analyses/55/models").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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

  @Test
  public void testUpdate() throws Exception {

    String modelTitle = "model title";
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 2;
    Integer inferenceIterations = 2;
    Integer thinningFactor = 2;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Integer modelId = 1;

    UpdateModelCommand updateModelCommand = new UpdateModelCommand.UpdateModelCommandBuilder()
            .setId(modelId)
            .setTitle(modelTitle)
            .setLinearModel(linearModel)
            .setModelTypeCommand(modelTypeCommand)
            .setBurnInIterations(burnInIterations)
            .setInferenceIterations(inferenceIterations)
            .setThinningFactor(thinningFactor)
            .setLikelihood(likelihood)
            .setLink(link)
            .build();
    String postBodyStr = TestUtils.createJson(updateModelCommand);

    mockMvc.perform(post("/projects/45/analyses/55/models/1")
            .content(postBodyStr)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    verify(modelService).checkOwnership(modelId, user);
    verify(modelService).increaseRunLength(updateModelCommand);
  }

  @Test
  public void setAttributes() throws Exception {
    String postBodyStr = TestUtils.createJson(new ModelAttributesCommand(true));
    mockMvc.perform(post("/projects/45/analyses/55/models/1/attributes")
            .content(postBodyStr)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    verify(modelService).checkOwnership(1, user);
    verify(modelRepository).setArchived(1, true);
  }

  @Test
  public void testGetResult() throws Exception {
    URI taskId = URI.create("2");
    Model model = modelBuilder.taskUri(taskId).build();
    Integer modelID = 1;
    JsonNode jsonNode = new ObjectMapper().readTree("{}");
    when(modelService.get(modelID)).thenReturn(model);
    when(pataviTaskRepository.getResult(model.getTaskUrl())).thenReturn(jsonNode);
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models/1/result").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()));
    verify(modelService).get(model.getId());
    verify(pataviTaskRepository).getResult(model.getTaskUrl());
  }

  @Test
  public void testGetResultNoTaskId() throws Exception {
    Model model = modelBuilder.build();
    Integer modelID = 1;
    when(modelService.get(modelID)).thenReturn(model);
    ResultActions resultActions = mockMvc.perform(get("/projects/45/analyses/55/models/1/result").principal(user));
    resultActions
            .andExpect(status().isNotFound());
    verify(modelService).get(model.getId());
  }

  @Test
  public void testConsistencyModels() throws Exception {
    Integer projectId = 1;
    when(modelService.queryConsistencyModels(projectId)).thenReturn(Collections.<Model>emptyList());
    ResultActions resultActions = mockMvc.perform(get("/projects/1/consistencyModels").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    verify(modelService).queryConsistencyModels(projectId);
  }

  @Test
  public void testQueryModelsByProject() throws Exception {
    Integer projectId = 1;
    when(modelService.queryModelsByProject(projectId)).thenReturn(Collections.<Model>emptyList());
    ResultActions resultActions = mockMvc.perform(get("/projects/1/models").principal(user));
    resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    verify(modelService).queryModelsByProject(projectId);
  }

  @Test
  public void testCreateFunnelPlot() throws Exception {
    CreateFunnelPlotComparisonCommand comparison1 = new CreateFunnelPlotComparisonCommand(251, 249, BiasDirection.T_2);
    CreateFunnelPlotComparisonCommand comparison2 = new CreateFunnelPlotComparisonCommand(253, 249, BiasDirection.T_2);
    CreateFunnelPlotComparisonCommand comparison3 = new CreateFunnelPlotComparisonCommand(252, 249, BiasDirection.T_2);
    CreateFunnelPlotComparisonCommand comparison4 = new CreateFunnelPlotComparisonCommand(250, 249, BiasDirection.T_2);
    List<CreateFunnelPlotComparisonCommand> includedComparisons = Arrays.asList(comparison1,
            comparison2,
            comparison3,
            comparison4);
    Integer modelId = 315;
    CreateFunnelPlotCommand createFunnelPlotCommand = new CreateFunnelPlotCommand(modelId, includedComparisons);
    String postBodyStr = TestUtils.createJson(createFunnelPlotCommand);
    MockHttpServletRequestBuilder post = post("/projects/1/analyses/2/models/3/funnelPlots")
            .content(postBodyStr)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON);
    mockMvc.perform(post).andExpect(status().isCreated());
    verify(analysisService).checkCoordinates(1, 2);
    verify(modelService).checkOwnership(3, user);
    verify(funnelPlotRepository).create(createFunnelPlotCommand);
  }

  @Test
  public void testQueryFunnelPlots() throws Exception {
    Integer modelId = 3;

    FunnelPlotComparison comparison1 = new FunnelPlotComparison(1, 2, 3, BiasDirection.T_1);
    FunnelPlotComparison comparison2 = new FunnelPlotComparison(1, 3, 4, BiasDirection.T_2);
    List<FunnelPlotComparison> includedComparisons = Arrays.asList(comparison1, comparison2);
    FunnelPlot funnelPlot = new FunnelPlot(1, 1, includedComparisons);

    when(funnelPlotRepository.query(modelId)).thenReturn(Collections.singletonList(funnelPlot));

    mockMvc.perform(get("/projects/1/analyses/2/models/3/funnelPlots").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$[0].id", equalTo(funnelPlot.getId())));

    verify(funnelPlotRepository).query(modelId);
  }

  @Test
  public void testGetModelBaseline() throws Exception {
    int modelId = 3;
    ModelBaseline modelBaseline = new ModelBaseline(modelId, "testBaseline");
    when(modelBaselineRepository.getModelBaseline(modelId)).thenReturn(modelBaseline);

    mockMvc.perform(get("/projects/1/analyses/2/models/3/baseline").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.modelId", equalTo(modelBaseline.getModelId())))
            .andExpect(jsonPath("$.baseline", equalTo(modelBaseline.getBaseline())));
    verify(modelBaselineRepository).getModelBaseline(modelId);
  }

  @Test
  public void testSetModelBaseline() throws Exception {
    NormalBaselineDistribution normalBaselineDistribution = new NormalBaselineDistribution("scale", 1.2, 3.3, "name", "dnorm");
    String putBodyStr = TestUtils.createJson(normalBaselineDistribution);
    MockHttpServletRequestBuilder put = put("/projects/1/analyses/2/models/3/baseline")
            .content(putBodyStr)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON);

    mockMvc.perform(put).andExpect(status().isOk());
    verify(modelService).checkOwnership(3, user);
    verify(modelBaselineRepository).setModelBaseline(3, putBodyStr);
  }

  @Test
  public void testSetModelTitle() throws Exception {
    Integer modelId = 3;
    String newTitle = "new title";
    String title = "{\"newTitle\": \"" + newTitle + "\"}";
    MockHttpServletRequestBuilder put = put("/projects/1/analyses/2/models/3/setTitle")
            .content(title)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON);

    mockMvc.perform(put).andExpect(status().isOk());
    verify(modelService).checkOwnership(modelId, user);
    verify(modelRepository).setTitle(modelId, newTitle);
  }
}