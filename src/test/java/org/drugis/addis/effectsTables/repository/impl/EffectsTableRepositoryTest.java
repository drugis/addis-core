package org.drugis.addis.effectsTables.repository.impl;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.effectsTables.EffectsTableAlternativeInclusion;
import org.drugis.addis.effectsTables.repository.EffectsTableRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by joris on 5-4-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class EffectsTableRepositoryTest {

  @Inject
  private EffectsTableRepository effectsTableRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  private Integer analysisId = -1;



  @Test
  public void testGetEffectsTable() throws Exception {
    List<EffectsTableAlternativeInclusion> result = effectsTableRepository.getEffectsTableAlternativeInclusions(analysisId);
    List<EffectsTableAlternativeInclusion> expectedResult = Arrays.asList(new EffectsTableAlternativeInclusion(-1, "1"), new EffectsTableAlternativeInclusion(-1, "2"));
    for (int i = 0; i < result.size(); i++) {
      assertEquals(result.get(i), expectedResult.get(i));
    }
  }

  @Test
  public void testSetEffectsTableExclusion() throws Exception {
    // add alternative with id 3, remove alternative with id 2
    effectsTableRepository.setEffectsTableAlternativeInclusion(analysisId,"3");
    effectsTableRepository.setEffectsTableAlternativeInclusion(analysisId,"2");
    List<EffectsTableAlternativeInclusion> result = effectsTableRepository.getEffectsTableAlternativeInclusions(analysisId);
    List<EffectsTableAlternativeInclusion> expectedResult = Arrays.asList(
            new EffectsTableAlternativeInclusion(analysisId, "1"), new EffectsTableAlternativeInclusion(analysisId, "3"));
    for (int i = 0; i < result.size(); i++) {
      assertEquals(result.get(i), expectedResult.get(i));
    }
  }

}