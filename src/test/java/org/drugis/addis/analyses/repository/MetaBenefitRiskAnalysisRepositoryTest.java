package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by daan on 25-2-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class MetaBenefitRiskAnalysisRepositoryTest {

  @Inject
  MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testQuery() {
    int projectId = 1;
    Collection<MetaBenefitRiskAnalysis> metaBRAnalyses = metaBenefitRiskAnalysisRepository.queryByProject(projectId);
    int primaryKey = -10;
    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = em.find(MetaBenefitRiskAnalysis.class, primaryKey);
    assertTrue(metaBRAnalyses.contains(metaBenefitRiskAnalysis));
    assertEquals(1, metaBRAnalyses.size());
  }

  @Test
  public void testCreate() {
    int projectId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new analysis", AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL);
    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = metaBenefitRiskAnalysisRepository.create(analysisCommand);
    assertNotNull(metaBenefitRiskAnalysis);
    assertNotNull(metaBenefitRiskAnalysis.getId());
  }

}