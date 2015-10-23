package org.drugis.addis.models.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class JpaModelRepositoryTest {

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  private ModelRepository modelRepository;

  @Test
  public void testCreate() throws Exception, InvalidModelTypeException, InvalidHeterogeneityTypeException {
    Integer analysisId = -5;
    String modelTitle = "model title";
    String linearModel = "fixed";
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Model toPersist = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .heterogeneityPriorType(Model.AUTOMATIC_HETEROGENEITY_PRIOR_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    Model model = modelRepository.persist(toPersist);
    assertEquals(analysisId, model.getAnalysisId());
    assertNotNull(model.getId());
    assertEquals("fixed", model.getLinearModel());
    assertEquals(Model.NETWORK_MODEL_TYPE, model.getModelTypeTypeAsString());
    assertEquals(Model.AUTOMATIC_HETEROGENEITY_PRIOR_TYPE, model.getHeterogeneityPrior().getType());
    assertEquals(burnInIterations, model.getBurnInIterations());
    assertEquals(inferenceIterations, model.getInferenceIterations());
    assertEquals(thinningFactor, model.getThinningFactor());
    assertEquals(likelihood, model.getLikelihood());
    assertEquals(link, model.getLink());
    assertNull(model.getPairwiseDetails());
  }

  @Test
  public void testGet() {
    Integer analysisId = -5;
    Integer modelId = 1;
    Model result = modelRepository.find(modelId);
    assertNotNull(result);
    assertEquals(analysisId, result.getAnalysisId());
  }

  @Test
  public void getPairwiseTypeModel() {
    Integer analysisId = -5;
    Integer modelId = 2;
    Double mean = 2.3;
    Double stdDev = 0.3;
    Model result = modelRepository.find(modelId);
    assertNotNull(result);
    assertEquals(analysisId, result.getAnalysisId());
    assertNotNull(result.getId());
    assertEquals("fixed", result.getLinearModel());
    assertEquals(Model.PAIRWISE_MODEL_TYPE, result.getModelTypeTypeAsString());
    assertNotNull(result.getPairwiseDetails());
    Pair<Model.DetailNode, Model.DetailNode> details = result.getPairwiseDetails();
    assertEquals("study2", details.getLeft().getName());
    assertEquals("study1", details.getRight().getName());
    assertEquals(Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE, result.getHeterogeneityPrior().getType());
    Model.HeterogeneityVarianceValues values = (Model.HeterogeneityVarianceValues) result.getHeterogeneityPrior().getValues();
    assertEquals(mean, values.getMean());
    assertEquals(stdDev, values.getStdDev());
  }

  @Test
  public void testFindByAnalysis() throws SQLException {
    Integer analysisId = -5;
    NetworkMetaAnalysis networkMetaAnalysisWithModel = em.find(NetworkMetaAnalysis.class, analysisId);
    List<Model> models = modelRepository.findByAnalysis(networkMetaAnalysisWithModel.getId());
    assertNotNull(models);
    assertEquals(2, models.size());
    assertEquals(networkMetaAnalysisWithModel.getId(), models.get(0).getAnalysisId());
    assertFalse(models.get(0).isHasResult());
    assertFalse(models.get(1).isHasResult());

    Integer analysisIdWithoutModel = -6;
    NetworkMetaAnalysis networkMetaAnalysisWithWithOutModel = em.find(NetworkMetaAnalysis.class, analysisIdWithoutModel);
    models = modelRepository.findByAnalysis(networkMetaAnalysisWithWithOutModel.getId());
    assertTrue(models.isEmpty());
  }

  @Test
  public void testFindByAnalysisWithTask() throws SQLException {
    Integer analysisId = -7;
    NetworkMetaAnalysis nmaTaskTest = em.find(NetworkMetaAnalysis.class, analysisId);
    List<Model> models = modelRepository.findByAnalysis(nmaTaskTest.getId());
    assertNotNull(models);
    assertEquals(2, models.size());
    assertTrue(models.get(0).isHasResult());
    assertFalse(models.get(1).isHasResult());

  }
}