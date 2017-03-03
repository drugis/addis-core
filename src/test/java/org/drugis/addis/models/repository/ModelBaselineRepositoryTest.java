package org.drugis.addis.models.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.models.ModelBaseline;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by daan on 3-3-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ModelBaselineRepositoryTest {

  @Inject
  ModelBaselineRepository modelBaselineRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testGet() {
    ModelBaseline modelBaseline = modelBaselineRepository.getModelBaseline(1);
    assertEquals((Integer)1, modelBaseline.getModelId());
    assertEquals("{\"type\": \"dnorm\"}", modelBaseline.getBaseline());
    assertEquals(null, modelBaselineRepository.getModelBaseline(3));
  }

  @Test
  public void testSet() {
    int modelId = 1;
    ModelBaseline modelBaseline = modelBaselineRepository.getModelBaseline(modelId);
    assertEquals("{\"type\": \"dnorm\"}", modelBaseline.getBaseline());
    modelBaselineRepository.setModelBaseline(modelId, "{\"scale\": \"enormous\"}");
    modelBaseline = modelBaselineRepository.getModelBaseline(modelId);
    assertEquals("{\"scale\": \"enormous\"}", modelBaseline.getBaseline());
    int modelId3 = 3;
    assertEquals(null, modelBaselineRepository.getModelBaseline(modelId3));
    modelBaselineRepository.setModelBaseline(modelId3, "newBaseline");
    assertEquals("newBaseline", modelBaselineRepository.getModelBaseline(modelId3).getBaseline());
  }

}