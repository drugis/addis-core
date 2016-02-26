package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.AnalysisType;
import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by daan on 25-2-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class MetaBenefitRiskAnalysisRepositoryTest {

  @Inject
  AnalysisService analysisService;

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
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    int accountId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new analysis", AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL);
    Account user = em.find(Account.class, accountId);
    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = metaBenefitRiskAnalysisRepository.create(user, analysisCommand);
    assertNotNull(metaBenefitRiskAnalysis);
    assertNotNull(metaBenefitRiskAnalysis.getId());
    assertEquals(2, metaBenefitRiskAnalysis.getIncludedAlternatives().size());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;
    int interventionId = 2;
    int outcomeId = 2;

    Account user = em.find(Account.class, accountId);
    MetaBenefitRiskAnalysis analysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    Intervention interventionToInclude = em.find(Intervention.class, interventionId);
    List<Intervention> interventions = new ArrayList<>(analysis.getIncludedAlternatives());
    interventions.add(interventionToInclude);
    analysis.setIncludedAlternatives(interventions);
    List<MbrOutcomeInclusion> mbrOutcomeInclusions = new ArrayList<>(analysis.getMbrOutcomeInclusions());
    Outcome outcomeToInclude = em.find(Outcome.class, outcomeId);
    mbrOutcomeInclusions.add(new MbrOutcomeInclusion(analysis.getId(), outcomeId, 1));
    analysis.setIncludedAlternatives(interventions);
    metaBenefitRiskAnalysisRepository.update(user, analysis);
    assertEquals(2, analysis.getIncludedAlternatives().size());
  }

}