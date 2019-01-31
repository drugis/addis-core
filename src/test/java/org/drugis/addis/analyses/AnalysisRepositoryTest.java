package org.drugis.addis.analyses;

import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by daan on 7-5-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class AnalysisRepositoryTest {

  @Inject
  private AnalysisRepository analysisRepository;

  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Test
  public void testGetNetworkMetaAnalysis() throws ResourceDoesNotExistException {
    Integer analysisId = -5;
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    NetworkMetaAnalysis castAnalysis = (NetworkMetaAnalysis) analysis;
    NetworkMetaAnalysis networkMetaAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(networkMetaAnalysis, castAnalysis);
  }

  @Test
  public void testGetBenefitRiskAnalysis() throws ResourceDoesNotExistException {
    Integer analysisId = -10;
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    BenefitRiskAnalysis castAnalysis = (BenefitRiskAnalysis) analysis;
    BenefitRiskAnalysis benefitRiskAnalysis = em.find(BenefitRiskAnalysis.class, analysisId);
    benefitRiskAnalysis = em.find(BenefitRiskAnalysis.class, benefitRiskAnalysis.getId());
    assertEquals(1, benefitRiskAnalysis.getInterventionInclusions().size());
    assertEquals(benefitRiskAnalysis, castAnalysis);
  }

  @Test
  public void testGetNetworkMetaAnalysisWithExcludedArms() throws ResourceDoesNotExistException {
    Integer analysisId = -6;
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    NetworkMetaAnalysis castAnalysis = (NetworkMetaAnalysis) analysis;
    NetworkMetaAnalysis networkMetaAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(networkMetaAnalysis, castAnalysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetNonexistentAnalysisFails() throws ResourceDoesNotExistException {
    analysisRepository.get(12345);
  }

  @Test
  public void testQuery() {
    Integer projectId = 1;
    List<AbstractAnalysis> analyses = analysisRepository.query(projectId);
    assertEquals(7, analyses.size());
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(-1, 1, "analysis 1", Collections.emptySet());
    assertTrue(analyses.contains(analysis));
  }

  @Test
  public void testArchiveAndUnArchiveProject() throws ResourceDoesNotExistException {
    int analysisId = -6;
    analysisRepository.setArchived(analysisId, true);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    assertEquals(true, analysis.getArchived());
    assertNotNull(analysis.getArchivedOn());
    analysisRepository.setArchived(analysisId, false);
    analysis = analysisRepository.get(analysisId);
    assertEquals(false, analysis.getArchived());
    assertNull(analysis.getArchivedOn());
  }
}
