package org.drugis.addis.models;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.repository.ModelRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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

  private Integer analysisId = -5;

  @Test
  public void testCreate() throws Exception, InvalidModelTypeException {
    String modelTitle = "model title";
    String linearModel = "fixed";
    String modelType = "network";
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Model toPersist = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(modelTitle)
            .linearModel(linearModel)
            .modelType(modelType)
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
    assertEquals(burnInIterations, model.getBurnInIterations());
    assertEquals(inferenceIterations, model.getInferenceIterations());
    assertEquals(thinningFactor, model.getThinningFactor());
    assertEquals(likelihood, model.getLikelihood());
    assertEquals(link, model.getLink());
    assertNull(model.getPairwiseDetails());
  }

  @Test
  public void testGet() {
    Integer modelId = 1;
    Model result = modelRepository.find(modelId);
    assertNotNull(result);
    assertEquals(analysisId, result.getAnalysisId());
  }

  @Test
  public void getPairwiseTypeModel() {
    Integer modelId = 2;
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
  }

  @Test
  public void testFindByAnalysis() {
    NetworkMetaAnalysis networkMetaAnalysisWithModel = em.find(NetworkMetaAnalysis.class, analysisId);
    List<Model> models = modelRepository.findByAnalysis(networkMetaAnalysisWithModel.getId());
    assertNotNull(models);
    assertEquals(2, models.size());
    assertEquals(networkMetaAnalysisWithModel.getId(), models.get(0).getAnalysisId());

    NetworkMetaAnalysis networkMetaAnalysisWithWithOutModel = em.find(NetworkMetaAnalysis.class, -6);
    models = modelRepository.findByAnalysis(networkMetaAnalysisWithWithOutModel.getId());
    assertTrue(models.isEmpty());
  }
}