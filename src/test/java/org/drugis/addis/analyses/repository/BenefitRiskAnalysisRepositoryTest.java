package org.drugis.addis.analyses.repository;

import com.google.common.collect.Sets;
import org.drugis.addis.analyses.model.*;
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
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

/**
 * Created by daan on 25-2-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class BenefitRiskAnalysisRepositoryTest {

  @Inject
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;

  @Inject
  private AnalysisService analysisService;

  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Test
  public void testFindMeta() {
    int analysisId = -10;
    BenefitRiskAnalysis benefitRiskAnalysis = benefitRiskAnalysisRepository.find(analysisId);
    Set<InterventionInclusion> interventionInclusions = Sets.newHashSet(new InterventionInclusion(analysisId, -1));
    BenefitRiskAnalysis expectedAnalysis = new BenefitRiskAnalysis(analysisId, 1, "metabr 1", interventionInclusions);
    List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions = Collections.singletonList(new BenefitRiskNMAOutcomeInclusion(analysisId, 1, -5, 1));
    expectedAnalysis.setBenefitRiskNMAOutcomeInclusions(outcomeInclusions);
    assertEquals(expectedAnalysis, benefitRiskAnalysis);
  }

  @Test
  public void testFindSingle() {
    int analysisId = -3;
    BenefitRiskAnalysis benefitRiskAnalysis = benefitRiskAnalysisRepository.find(analysisId);
    Set<InterventionInclusion> interventionInclusions = Sets.newHashSet(
            new InterventionInclusion(analysisId, -1),
            new InterventionInclusion(analysisId, -2));
    BenefitRiskAnalysis expectedAnalysis = new BenefitRiskAnalysis(analysisId, 1, "analysis 3", interventionInclusions);

    List<BenefitRiskStudyOutcomeInclusion> outcomeInclusions = Arrays.asList(
            new BenefitRiskStudyOutcomeInclusion(analysisId, 1, URI.create("http://study.graph.uri")),
            new BenefitRiskStudyOutcomeInclusion(analysisId, 2, URI.create("http://study.graph.uri")));
    expectedAnalysis.setBenefitRiskStudyOutcomeInclusions(outcomeInclusions);
    assertEquals(expectedAnalysis, benefitRiskAnalysis);
  }

  @Test
  public void testFindHybrid() {
  int analysisId = -2;
    BenefitRiskAnalysis benefitRiskAnalysis = benefitRiskAnalysisRepository.find(analysisId);
    Set<InterventionInclusion> interventionInclusions = Sets.newHashSet(
            new InterventionInclusion(analysisId, -1),
            new InterventionInclusion(analysisId, -2));
    BenefitRiskAnalysis expectedAnalysis = new BenefitRiskAnalysis(analysisId, 1, "analysis 2", interventionInclusions);

    List<BenefitRiskStudyOutcomeInclusion> studyOutcomeInclusions = Collections.singletonList(
            new BenefitRiskStudyOutcomeInclusion(analysisId, 1, URI.create("http://study.graph.uri")));
    List<BenefitRiskNMAOutcomeInclusion>  nMAOutcomeInclusions = Collections.singletonList(
            new BenefitRiskNMAOutcomeInclusion(analysisId, 1,-5,1));
    expectedAnalysis.setBenefitRiskStudyOutcomeInclusions(studyOutcomeInclusions);
    expectedAnalysis.setBenefitRiskNMAOutcomeInclusions(nMAOutcomeInclusions);
    assertEquals(expectedAnalysis, benefitRiskAnalysis);
  }

  @Test
  public void testQuery() {
    int projectId = 1;
    Collection<BenefitRiskAnalysis> brAnalyses = benefitRiskAnalysisRepository.queryByProject(projectId);
    int primaryKey = -10;
    BenefitRiskAnalysis metaBenefitRiskAnalysis = em.find(BenefitRiskAnalysis.class, primaryKey);
    assertTrue(brAnalyses.contains(metaBenefitRiskAnalysis));
    assertEquals(4, brAnalyses.size());
  }

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    int projectId = 1;
    int accountId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new analysis", AnalysisType.BENEFIT_RISK_ANALYSIS_LABEL);
    Account user = em.find(Account.class, accountId);

    BenefitRiskAnalysis metaBenefitRiskAnalysis = benefitRiskAnalysisRepository.create(user, analysisCommand);

    assertNotNull(metaBenefitRiskAnalysis);
    assertNotNull(metaBenefitRiskAnalysis.getId());
    assertEquals(0, metaBenefitRiskAnalysis.getInterventionInclusions().size());
    assertEquals(0, metaBenefitRiskAnalysis.getBenefitRiskNMAOutcomeInclusions().size());
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
    BenefitRiskAnalysis analysis = em.find(BenefitRiskAnalysis.class, analysisId);
    SimpleIntervention interventionToInclude = em.find(SimpleIntervention.class, interventionId);
    InterventionInclusion interventionInclusion = new InterventionInclusion(analysisId, interventionToInclude.getId());
    List<InterventionInclusion> interventions = new ArrayList<>(analysis.getInterventionInclusions());
    interventions.add(interventionInclusion);
    analysis.updateIncludedInterventions(new HashSet<>(interventions));
    List<BenefitRiskNMAOutcomeInclusion> benefitRiskNMAOutcomeInclusions = new ArrayList<>(analysis.getBenefitRiskNMAOutcomeInclusions());
    benefitRiskNMAOutcomeInclusions.add(new BenefitRiskNMAOutcomeInclusion(analysis.getId(), outcomeId, nmaId, modelId));
    analysis.updateIncludedInterventions(new HashSet<>(interventions));
    analysis.setBenefitRiskNMAOutcomeInclusions(benefitRiskNMAOutcomeInclusions);
    benefitRiskAnalysisRepository.update(user, analysis);
    BenefitRiskAnalysis updatedAnalysis = em.find(BenefitRiskAnalysis.class, analysisId);
    assertEquals(2, updatedAnalysis.getInterventionInclusions().size());
    assertEquals(2, updatedAnalysis.getBenefitRiskNMAOutcomeInclusions().size());
  }

  @Test
  public void testUpdateRemovesMBROutcomeInclusions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;
    int outcomeId = 2;
    int modelId = 1;

    Account user = em.find(Account.class, accountId);
    BenefitRiskAnalysis analysis = em.find(BenefitRiskAnalysis.class, analysisId);
    BenefitRiskNMAOutcomeInclusion newInclusion1 = new BenefitRiskNMAOutcomeInclusion(analysis.getId(), outcomeId, -6, modelId);
    BenefitRiskNMAOutcomeInclusion newInclusion2 = new BenefitRiskNMAOutcomeInclusion(analysis.getId(), outcomeId, -8, modelId);
    List<BenefitRiskNMAOutcomeInclusion> benefitRiskNMAOutcomeInclusions = Arrays.asList(newInclusion1, newInclusion2);
    analysis.setBenefitRiskNMAOutcomeInclusions(benefitRiskNMAOutcomeInclusions);
    benefitRiskAnalysisRepository.update(user, analysis);
    BenefitRiskAnalysis updatedAnalysis = em.find(BenefitRiskAnalysis.class, analysisId);
    assertEquals(Sets.newHashSet(Arrays.asList(newInclusion1, newInclusion2)), Sets.newHashSet(updatedAnalysis.getBenefitRiskNMAOutcomeInclusions()));

    TypedQuery<BenefitRiskNMAOutcomeInclusion> query = em.createQuery("FROM BenefitRiskNMAOutcomeInclusion WHERE analysisId = :analysisId", BenefitRiskNMAOutcomeInclusion.class);
    query.setParameter("analysisId", analysisId);
    List<BenefitRiskNMAOutcomeInclusion> resultList = query.getResultList();
    assertEquals(2, resultList.size());
  }

  @Test
  public void testUpdateMetaBrBaseLine() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int accountId = 1;
    int analysisId = -10;

    Account user = em.find(Account.class, accountId);
    BenefitRiskAnalysis analysis = em.find(BenefitRiskAnalysis.class, analysisId);

    ArrayList<BenefitRiskNMAOutcomeInclusion> benefitRiskNMAOutcomeInclusions = new ArrayList<>(analysis.getBenefitRiskNMAOutcomeInclusions());
    String baseline = "{outcome:{a:'b'}, mean:-0.1, stdDev:0.001)";
    benefitRiskNMAOutcomeInclusions.get(0).setBaseline(baseline);

    analysis.setBenefitRiskNMAOutcomeInclusions(benefitRiskNMAOutcomeInclusions);
    BenefitRiskAnalysis updated = benefitRiskAnalysisRepository.update(user, analysis);
    assertEquals(1, updated.getBenefitRiskNMAOutcomeInclusions().size());
    assertEquals(baseline, updated.getBenefitRiskNMAOutcomeInclusions().get(0).getBaseline());
  }

  @Test
  public void testRemoveAnalysis() {
    int analysisId = -10;

    BenefitRiskAnalysis analysis = em.find(BenefitRiskAnalysis.class, analysisId);

    em.remove(analysis);
    em.flush();

    SimpleIntervention intervention = em.find(SimpleIntervention.class, -1);
    em.flush();
    assertNotNull(intervention);
  }

}