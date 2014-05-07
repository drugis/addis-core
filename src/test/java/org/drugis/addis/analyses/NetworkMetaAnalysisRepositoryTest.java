package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.*;

/**
 * Created by daan on 7-5-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class NetworkMetaAnalysisRepositoryTest {

  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "newName", AnalysisType.NETWORK_META_ANALYSIS_LABEL);
    Account user = em.find(Account.class, 1);
    NetworkMetaAnalysis analysis = networkMetaAnalysisRepository.create(user, analysisCommand);
    assertNotNull(analysis);
    NetworkMetaAnalysis expectedAnalysis = em.find(NetworkMetaAnalysis.class, analysis.getId());
    assertEquals(expectedAnalysis, analysis);
  }
}
