package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
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
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 3/11/14.
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
  public void testQuery() {
    Collection<Analysis> analyses = analysisRepository.query(1);
    assertEquals(2, analyses.size());
    analyses = analysisRepository.query(2);
    assertEquals(1, analyses.size());
  }

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "newName", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
    Account user = em.find(Account.class, 1);
    Analysis result = analysisRepository.create(user, analysisCommand);
    assertTrue(analysisRepository.query(1).contains(result));
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Analysis analysis = analysisRepository.get(1, 1);
    assertEquals(em.find(Analysis.class, 1), analysis);
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    Account user = em.find(Account.class, 1);
    AnalysisCommand analysisCommand = new AnalysisCommand(1, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(1, 2, 3));
    Analysis updatedAnalysis = analysisRepository.update(user, analysisId, analysisCommand);
    assertEquals(analysisCommand.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysisCommand.getName(), updatedAnalysis.getName());
    assertEquals(analysisCommand.getType(), updatedAnalysis.getAnalysisType().getLabel());
    assertEquals(analysisCommand.getSelectedOutcomeIds().get(0), updatedAnalysis.getSelectedOutcomes().get(0).getId());
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    AnalysisCommand analysisCommand = new AnalysisCommand(2, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(1, 2, 3));
    analysisRepository.update(user, 1, analysisCommand);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateNotOwnedProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    AnalysisCommand analysisCommand = new AnalysisCommand(2, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(1, 2, 3));
    analysisRepository.update(user, 3, analysisCommand);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    analysisRepository.get(2, 1);
  }


}
