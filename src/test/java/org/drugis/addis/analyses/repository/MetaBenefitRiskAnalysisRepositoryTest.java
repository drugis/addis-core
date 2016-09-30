package org.drugis.addis.analyses.repository;

import com.google.common.collect.Sets;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by daan on 25-2-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class MetaBenefitRiskAnalysisRepositoryTest {

  @Inject
  MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Inject
  AnalysisService analysisService;

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
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    int projectId = 1;
    int accountId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new analysis", AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL);
    Account user = em.find(Account.class, accountId);
    when(analysisService.buildInitialOutcomeInclusions(any(), any())).thenReturn(Collections.emptyList());

    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = metaBenefitRiskAnalysisRepository.create(user, analysisCommand);

    assertNotNull(metaBenefitRiskAnalysis);
    assertNotNull(metaBenefitRiskAnalysis.getId());
    assertEquals(2, metaBenefitRiskAnalysis.getInterventionInclusions().size());
    assertEquals(0, metaBenefitRiskAnalysis.getMbrOutcomeInclusions().size());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;
    int interventionId = -2;
    int outcomeId = 2;
    int modelId = 1;
    Integer nmaId = -6;

    Account user = em.find(Account.class, accountId);
    MetaBenefitRiskAnalysis analysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    SimpleIntervention interventionToInclude = em.find(SimpleIntervention.class, interventionId);
    InterventionInclusion interventionInclusion = new InterventionInclusion(analysisId, interventionToInclude.getId());
    List<InterventionInclusion> interventions = new ArrayList<>(analysis.getInterventionInclusions());
    interventions.add(interventionInclusion);
    analysis.updateIncludedInterventions(new HashSet<>(interventions));
    List<MbrOutcomeInclusion> mbrOutcomeInclusions = new ArrayList<>(analysis.getMbrOutcomeInclusions());
    mbrOutcomeInclusions.add(new MbrOutcomeInclusion(analysis.getId(), outcomeId, nmaId, modelId));
    analysis.updateIncludedInterventions(new HashSet<>(interventions));
    analysis.setMbrOutcomeInclusions(mbrOutcomeInclusions);
    metaBenefitRiskAnalysisRepository.update(user, analysis);
    MetaBenefitRiskAnalysis updatedAnalysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    assertEquals(2, updatedAnalysis.getInterventionInclusions().size());
    assertEquals(2, updatedAnalysis.getMbrOutcomeInclusions().size());
  }

  @Test
  public void testUpdateRemovesMBROutcomeInclusions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;
    int outcomeId = 2;
    int modelId = 1;

    Account user = em.find(Account.class, accountId);
    MetaBenefitRiskAnalysis analysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    MbrOutcomeInclusion newInclusion1 = new MbrOutcomeInclusion(analysis.getId(), outcomeId, -6, modelId);
    MbrOutcomeInclusion newInclusion2 = new MbrOutcomeInclusion(analysis.getId(), outcomeId, -8, modelId);
    List<MbrOutcomeInclusion> mbrOutcomeInclusions = Arrays.asList(newInclusion1, newInclusion2);
    analysis.setMbrOutcomeInclusions(mbrOutcomeInclusions);
    metaBenefitRiskAnalysisRepository.update(user, analysis);
    MetaBenefitRiskAnalysis updatedAnalysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    assertEquals(Sets.newHashSet(Arrays.asList(newInclusion1, newInclusion2)), Sets.newHashSet(updatedAnalysis.getMbrOutcomeInclusions()));

    TypedQuery<MbrOutcomeInclusion> query = em.createQuery("FROM MbrOutcomeInclusion WHERE metaBenefitRiskAnalysisId = :analysisId", MbrOutcomeInclusion.class);
    query.setParameter("analysisId", analysisId);
    List<MbrOutcomeInclusion> resultList = query.getResultList();
    assertEquals(2, resultList.size());
  }

  @Test
  public void testUpdateMetaBrBaseLine() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;

    Account user = em.find(Account.class, accountId);
    MetaBenefitRiskAnalysis analysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);

    ArrayList<MbrOutcomeInclusion> mbrOutcomeInclusions = new ArrayList<>(analysis.getMbrOutcomeInclusions());
    String baseline = "{outcome:{a:'b'}, mean:-0.1, stdDev:0.001)";
    mbrOutcomeInclusions.get(0).setBaseline(baseline);

    analysis.setMbrOutcomeInclusions(mbrOutcomeInclusions);
    MetaBenefitRiskAnalysis updated = metaBenefitRiskAnalysisRepository.update(user, analysis);
    assertEquals(1, updated.getMbrOutcomeInclusions().size());
    assertEquals(baseline, updated.getMbrOutcomeInclusions().get(0).getBaseline());
  }

  @Test
  public void testRemoveAnalysis() {
    int analysisId = -10;

    MetaBenefitRiskAnalysis analysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);

    em.remove(analysis);
    em.flush();

    SimpleIntervention intervention = em.find(SimpleIntervention.class, -1);
    em.flush();
    assertNotNull(intervention);
  }

}