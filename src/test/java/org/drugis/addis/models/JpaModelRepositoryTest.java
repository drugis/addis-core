package org.drugis.addis.models;

import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.config.JpaRepositoryTestConfig;
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

  @Test
  public void testCreate() throws Exception {
    Integer analysisId = -5; // from test-data/sql
    String modelTitle = "model title";
    String linearModel = "fixed";
    Model model = modelRepository.create(analysisId, modelTitle, linearModel);
    assertEquals(analysisId, model.getAnalysisId());
    assertNotNull(model.getId());
  }

  @Test
  public void testGet() {
    Integer modelId = 1;
    Integer analysisId = -5;
    Model result = modelRepository.find(modelId);
    assertNotNull(result);
    assertEquals(analysisId, result.getAnalysisId());
  }

  @Test
  public void testFindByAnalysis() {
    NetworkMetaAnalysis networkMetaAnalysisWithModel = em.find(NetworkMetaAnalysis.class, -5);
    List<Model> models = modelRepository.findByAnalysis(networkMetaAnalysisWithModel.getId());
    assertNotNull(models);
    assertEquals(networkMetaAnalysisWithModel.getId(), models.get(0).getAnalysisId());

    NetworkMetaAnalysis networkMetaAnalysisWithWithOutModel = em.find(NetworkMetaAnalysis.class, -6);
    models = modelRepository.findByAnalysis(networkMetaAnalysisWithWithOutModel.getId());
    assertTrue(models.isEmpty());
  }
}