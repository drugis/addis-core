package org.drugis.addis.analyses;

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

import static org.junit.Assert.assertEquals;

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
  EntityManager em;

  @Test
  public void testGetSingleStudyBenefitRiskAnalysis() throws ResourceDoesNotExistException {
    Integer projectId = 1;
    Integer analysisId = -4;
    AbstractAnalysis analysis = analysisRepository.get(projectId, analysisId);
    SingleStudyBenefitRiskAnalysis castAnalysis = (SingleStudyBenefitRiskAnalysis) analysis;
    SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(singleStudyBenefitRiskAnalysis, castAnalysis);
  }

  @Test
  public void testGetNetworkMetaAnalysis() throws ResourceDoesNotExistException {
    Integer projectId = 1;
    Integer analysisId = -5;
    AbstractAnalysis analysis = analysisRepository.get(projectId, analysisId);
    NetworkMetaAnalysis castAnalysis = (NetworkMetaAnalysis) analysis;
    NetworkMetaAnalysis networkMetaAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(networkMetaAnalysis, castAnalysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetNonexistentAnalysisFails() throws ResourceDoesNotExistException {
    analysisRepository.get(1, 12345);
  }

}
