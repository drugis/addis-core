package org.drugis.addis.models.service;

import com.google.common.collect.Sets;
import net.minidev.json.JSONObject;
import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.controller.command.*;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.impl.ModelServiceImpl;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

;

/**
 * Created by daan on 24-6-14.
 */
public class ModelServiceTest {

  @Mock
  private ModelRepository modelRepository;

  @Mock
  private AnalysisRepository analysisRepository;

  @Mock
  private ProjectService projectService;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @InjectMocks
  private ModelService modelService;

  private Model.ModelBuilder modelBuilder;
  private final Integer projectId = 1;
  private final Integer analysisId = 55;


  @Before
  public void setUp() throws Exception {
    modelService = new ModelServiceImpl();
    MockitoAnnotations.initMocks(this);

    // some default values to use as a basis for tests
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    modelBuilder = new Model.ModelBuilder(analysisId, modelTitle)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link);

  }

  @Test
  public void testCreateNetwork() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand modelCommand = new CreateModelCommand.CreateModelCommandBuilder()
            .setTitle(modelTitle)
            .setLinearModel(linearModel)
            .setModelType(modelTypeCommand)
            .setHeterogeneityPriorCommand(heterogeneityPriorCommand)
            .setBurnInIterations(burnInIterations)
            .setInferenceIterations(inferenceIterations)
            .setThinningFactor(thinningFactor)
            .setLikelihood(likelihood)
            .setLink(link)
            .build();
    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder.build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, modelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }

  @Test
  public void testCreateHeterogeneityStdDevNetwork() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_RANDOM;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    Double lower = 0.5;
    Double upper = 1.0;
    HeterogeneityPriorCommand heterogeneityPriorCommand = new StdDevHeterogeneityPriorCommand(new StdDevValuesCommand(lower, upper));

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder
            .linearModel(linearModel)
            .heterogeneityPriorType(Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE)
            .lower(lower)
            .upper(upper)
            .build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, createModelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }

  @Test
  public void testCreateHeterogeneityVarianceNetwork() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    Double mean = 2.3;
    Double stdDev = 0.3;
    HeterogeneityPriorCommand heterogeneityPriorCommand = new VarianceHeterogeneityPriorCommand(new VarianceValuesCommand(mean, stdDev));

    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder
            .modelType(Model.NETWORK_MODEL_TYPE)
            .heterogeneityPriorType(Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE)
            .mean(mean)
            .stdDev(stdDev)
            .build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, createModelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }

  @Test
  public void testCreateHeterogeneityPrecisionNetwork() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    Double rate = 0.9;
    Double shape = 1.3;
    HeterogeneityPriorCommand heterogeneityPriorCommand = new PrecisionHeterogeneityPriorCommand(new PrecisionValuesCommand(rate, shape));

    CreateModelCommand modelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setHeterogeneityPriorCommand(heterogeneityPriorCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();
    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder
            .heterogeneityPriorType(Model.PRECISION_HETEROGENEITY_PRIOR_TYPE)
            .rate(rate)
            .shape(shape)
            .build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, modelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }

  @Test
  public void testCreatePairwise() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    int fromId = -1;
    String fromName = "fromName";
    NodeCommand from = new NodeCommand(fromId, fromName);
    int toId = -2;
    String toName = "toName";
    NodeCommand to = new NodeCommand(toId, toName);
    DetailsCommand details = new DetailsCommand(from, to);

    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("pairwise", details);
    CreateModelCommand createModelCommand = new CreateModelCommand.CreateModelCommandBuilder().setTitle(modelTitle).setLinearModel(linearModel).setModelType(modelTypeCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();

    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder
            .modelType(Model.PAIRWISE_MODEL_TYPE)
            .from(new Model.DetailNode(fromId, fromName))
            .to(new Model.DetailNode(toId, toName))
            .build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, createModelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }

  @Test
  public void testCreateMetaRegression() throws InvalidModelException, ResourceDoesNotExistException {
    Integer analysisId = 55;
    String modelTitle = "model title";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    JSONObject regressor = new JSONObject();
    regressor.put("a", "b");
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand(Model.REGRESSION_MODEL_TYPE, null);
    HeterogeneityPriorCommand heterogeneityPriorCommand = null;

    CreateModelCommand modelCommand = new CreateModelCommand.CreateModelCommandBuilder()
            .setTitle(modelTitle)
            .setLinearModel(linearModel)
            .setModelType(modelTypeCommand)
            .setHeterogeneityPriorCommand(heterogeneityPriorCommand)
            .setBurnInIterations(burnInIterations)
            .setInferenceIterations(inferenceIterations)
            .setThinningFactor(thinningFactor)
            .setLikelihood(likelihood)
            .setLink(link)
            .setRegressor(regressor)
            .build();
    Model expectedModel = mock(Model.class);

    Model internalModel = modelBuilder
            .modelType(Model.REGRESSION_MODEL_TYPE)
            .regressor(regressor)
            .build();

    when(modelRepository.persist(internalModel)).thenReturn(expectedModel);
    Model createdModel = modelService.createModel(analysisId, modelCommand);

    assertEquals(expectedModel, createdModel);
    verify(modelRepository).persist(internalModel);
  }


  @Test
  public void testQueryModelIsPresent() throws Exception {
    Integer analysisId = -1;
    Model model = modelBuilder
            .taskUri(URI.create("https://patavi.drugis.org/task/id1"))
            .build();

    List<Model> models = Collections.singletonList(model);
    PataviTask pataviTask = new PataviTask(TestUtils.buildPataviTaskJson("id1"));
    List<PataviTask> tasksByUri = Collections.singletonList(pataviTask);
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(models);
    when(pataviTaskRepository.findByUrls(Collections.singletonList(model.getTaskUrl()))).thenReturn(tasksByUri);

    List<Model> resultList = modelService.query(analysisId);

    assertEquals(1, resultList.size());
    assertEquals(models.get(0), resultList.get(0));
  }


  @Test
  public void testQueryModelIsNotPresent() throws Exception {
    Integer analysisId = -1;
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(new ArrayList<>());
    List<Model> resultList = modelService.query(analysisId);
    assertEquals(0, resultList.size());
  }

  @Test
  public void testCheckOwnership() throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelException, IOException {
    Integer modelId = 1;
    Integer analysisId = 2;
    Integer projectId = 3;
    Principal pricipal = mock(Principal.class);
    Model model = mock(Model.class);
    AbstractAnalysis analysis = mock(AbstractAnalysis.class);

    when(model.getAnalysisId()).thenReturn(analysisId);
    when(analysis.getProjectId()).thenReturn(projectId);
    when(modelRepository.get(modelId)).thenReturn(model);
    when(analysisRepository.get(analysisId)).thenReturn(analysis).thenReturn(analysis);

    modelService.checkOwnership(modelId, pricipal);

    verify(modelRepository).get(modelId);
    verify(analysisRepository).get(analysisId);
    verify(projectService).checkOwnership(projectId, pricipal);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testCheckOwnershipOnNonOwnedModel() throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelException, IOException {
    Integer modelId = 1;
    Integer analysisId = 2;
    Integer projectId = 3;
    Principal pricipal = mock(Principal.class);
    Model model = mock(Model.class);
    AbstractAnalysis analysis = mock(AbstractAnalysis.class);

    when(model.getAnalysisId()).thenReturn(analysisId);
    when(analysis.getProjectId()).thenReturn(projectId);
    when(modelRepository.get(modelId)).thenReturn(model);
    when(analysisRepository.get(analysisId)).thenReturn(analysis).thenReturn(analysis);
    Mockito.doThrow(new MethodNotAllowedException()).when(projectService).checkOwnership(projectId, pricipal);

    modelService.checkOwnership(modelId, pricipal);

    verify(modelRepository).get(modelId);
    verify(analysisRepository).get(analysisId);
    verify(projectService).checkOwnership(projectId, pricipal);
  }

  @Test
  public void testyIncreaseRunLength() throws InvalidModelException, MethodNotAllowedException, IOException {
    Integer modelId = 1;
    String modelTitle = "new title";
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 2;
    Integer inferenceIterations = 2;
    Integer thinningFactor = 2;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    String title = "title";

    UpdateModelCommand updateModelCommand = new UpdateModelCommand.UpdateModelCommandBuilder().setId(modelId).setTitle(modelTitle).setLinearModel(linearModel).setModelTypeCommand(modelTypeCommand).setBurnInIterations(burnInIterations).setInferenceIterations(inferenceIterations).setThinningFactor(thinningFactor).setLikelihood(likelihood).setLink(link).build();

    Model oldModel = new Model.ModelBuilder(2, title)
            .linearModel(Model.LINEAR_MODEL_RANDOM)
            .modelType(modelTypeCommand.getType())
            .burnInIterations(burnInIterations - 1)
            .inferenceIterations(inferenceIterations - 1)
            .thinningFactor(999)
            .likelihood(Model.LIKELIHOOD_NORMAL)
            .link(Model.LINK_CLOGLOG)
            .build();
    when(modelRepository.get(modelId)).thenReturn(oldModel);

    modelService.increaseRunLength(updateModelCommand);

    verify(modelRepository).get(modelId);

    oldModel.setBurnInIterations(updateModelCommand.getBurnInIterations());
    oldModel.setInferenceIterations(updateModelCommand.getInferenceIterations());
    oldModel.setThinningFactor(updateModelCommand.getThinningFactor());
    verify(modelRepository).persist(oldModel);
    verify(pataviTaskRepository).delete(oldModel.getTaskUrl());
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testyIncreaseRunLengthWithInvalidSettings() throws InvalidModelException, MethodNotAllowedException, IOException {
    Integer modelId = 1;
    String modelTitle = "new title";
    ModelTypeCommand modelTypeCommand = new ModelTypeCommand("network", null);
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 2;
    Integer inferenceIterations = 2;
    Integer thinningFactor = 2;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    String title = "title";

    UpdateModelCommand updateModelCommand = new UpdateModelCommand
            .UpdateModelCommandBuilder()
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

    Model oldModel = new Model.ModelBuilder(2, title)
            .linearModel(Model.LINEAR_MODEL_RANDOM)
            .modelType(modelTypeCommand.getType())
            .burnInIterations(burnInIterations + 1)
            .inferenceIterations(inferenceIterations + 1)
            .thinningFactor(999)
            .likelihood(Model.LIKELIHOOD_NORMAL)
            .link(Model.LINK_CLOGLOG)
            .build();
    when(modelRepository.get(modelId)).thenReturn(oldModel);

    modelService.increaseRunLength(updateModelCommand);

    verify(modelRepository).get(modelId);
    verifyNoMoreInteractions(modelRepository);
    verifyNoInteractions(pataviTaskRepository);
  }

  @Test
  public void testQueryModelsByProject() throws InvalidModelException, SQLException, IOException {
    String modelTitle = "title";
    Model networkTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build();
    URI taskUri = URI.create("https://patavi.drugis.org/task/taskId1");
    Model pairwiseTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY)
            .modelType(Model.PAIRWISE_MODEL_TYPE).from(new Model.DetailNode(1, "from")).to(new Model.DetailNode(2, "to"))
            .taskUri(taskUri)
            .build();
    Model otherTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY).modelType(Model.NODE_SPLITTING_MODEL_TYPE)
            .from(new Model.DetailNode(1, "from")).to(new Model.DetailNode(2, "to"))
            .build();
    List<Model> projectModels = Arrays.asList(networkTypeModel, pairwiseTypeModel, otherTypeModel);
    List<PataviTask> pataviTask = Collections.singletonList(new PataviTask(TestUtils.buildPataviTaskJson("taskId1")));
    when(pataviTaskRepository.findByUrls(Collections.singletonList(taskUri))).thenReturn(pataviTask);
    when(modelRepository.findModelsByProject(projectId)).thenReturn(projectModels);

    List<Model> result = modelService.queryModelsByProject(projectId);

    assertTrue(result.contains(pairwiseTypeModel));
    assertTrue(result.contains(networkTypeModel));
    assertTrue(result.contains(otherTypeModel));
  }

  @Test
  public void testQueryConsistencyModels() throws InvalidModelException, SQLException, IOException {
    String modelTitle = "title";
    Model networkTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build();
    URI taskUri = URI.create("https://patavi.drugis.org/task/taskId1");
    Model pairwiseTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY)
            .modelType(Model.PAIRWISE_MODEL_TYPE).from(new Model.DetailNode(1, "from")).to(new Model.DetailNode(2, "to"))
            .taskUri(taskUri)
            .build();
    Model otherTypeModel = new Model.ModelBuilder(analysisId, modelTitle).link(Model.LINK_IDENTITY).modelType(Model.NODE_SPLITTING_MODEL_TYPE)
            .from(new Model.DetailNode(1, "from")).to(new Model.DetailNode(2, "to"))
            .build();
    List<Model> projectModels = Arrays.asList(networkTypeModel, pairwiseTypeModel, otherTypeModel);
    List<PataviTask> pataviTask = Collections.singletonList(new PataviTask(TestUtils.buildPataviTaskJson("taskId1")));
    when(pataviTaskRepository.findByUrls(Collections.singletonList(taskUri))).thenReturn(pataviTask);
    when(modelRepository.findModelsByProject(projectId)).thenReturn(projectModels);

    List<Model> result = modelService.queryConsistencyModels(projectId);

    assertTrue(result.contains(pairwiseTypeModel));
    assertTrue(result.contains(networkTypeModel));
    assertFalse(result.contains(otherTypeModel));
  }

  @Test
  public void testGetModels() throws IOException, SQLException, InvalidModelException {
    Integer modelId1 = -1;
    Integer modelId2 = -2;
    Set<Integer> modelIds = Sets.newHashSet(modelId1, modelId2);
    Integer analysisId1 = -10;
    URI taskUri = URI.create("https://patavi.drugis.org/task/taskId1");
    Model modelWithTask = new Model.ModelBuilder(analysisId1, "model 1")
            .link(Model.LINK_IDENTITY)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .taskUri(taskUri)
            .build();
    Model modelWithoutTask = new Model.ModelBuilder(analysisId1, "model 2").link(Model.LINK_IDENTITY).modelType(Model.NETWORK_MODEL_TYPE).build();
    List<Model> models = Arrays.asList(modelWithTask, modelWithoutTask);
    when(modelRepository.get(modelIds)).thenReturn(models);
    List<PataviTask> pataviTask = Collections.singletonList(new PataviTask(TestUtils.buildPataviTaskJson("taskId1")));
    when(pataviTaskRepository.findByUrls(Collections.singletonList(taskUri))).thenReturn(pataviTask);

    List<Model> resultModels = modelService.get(modelIds);
    assertEquals(2, resultModels.size());
    assertEquals("done", models.get(0).getRunStatus());
    assertNull(models.get(1).getRunStatus());
  }

}
