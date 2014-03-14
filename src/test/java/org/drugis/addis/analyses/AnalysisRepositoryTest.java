package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    Integer projectId = 1;
    Analysis analysis = new Analysis(analysisId, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST);
    Analysis updatedAnalysis = analysisRepository.update(user, analysisId, analysis);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
    assertEquals(analysis.getAnalysisType(), updatedAnalysis.getAnalysisType());
    assertEquals(analysis.getSelectedOutcomes(), updatedAnalysis.getSelectedOutcomes());
  }

  @Test
  public void testUpdateWithOutcomes() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    Object outcomeId = 1;
    List<Outcome> selectedOutcomes = Arrays.asList(em.find(Outcome.class, outcomeId));
    Analysis analysis = new Analysis(analysisId, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, selectedOutcomes);
    Analysis updatedAnalysis = analysisRepository.update(user, analysisId, analysis);
    Analysis result = em.find(Analysis.class, analysisId);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
    assertEquals(analysis.getAnalysisType(), updatedAnalysis.getAnalysisType());
    assertEquals(analysis.getSelectedOutcomes(), updatedAnalysis.getSelectedOutcomes());
    assertEquals(updatedAnalysis, result);
  }

//  @Test(expected = ResourceDoesNotExistException.class)
//  public void testUpdateInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
//    Account user = em.find(Account.class, 1);
//    int projectId = 3;
//    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(1, 2));
//    analysisRepository.update(user, projectId, analysisCommand);
//  }
//
//  @Test(expected = MethodNotAllowedException.class)
//  public void testUpdateNotOwnedProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
//    Account user = em.find(Account.class, 1);
//    int notOwnedProjectId = 2;
//    AnalysisCommand analysisCommand = new AnalysisCommand(notOwnedProjectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(1, 2));
//    int analysisId = 3;
//    analysisRepository.update(user, analysisId, analysisCommand);
//  }
//
//  @Test(expected = ResourceDoesNotExistException.class)
//  public void testUpdateAnalysisWithNonProjectOutcome() throws ResourceDoesNotExistException, MethodNotAllowedException {
//    Account user = em.find(Account.class, 2);
//    int projectId = 2;
//    int analysisId = 3;
//    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL, Arrays.asList(2));
//    analysisRepository.update(user, analysisId, analysisCommand);
//  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    analysisRepository.get(2, 1);
  }


}
