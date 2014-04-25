package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
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
    assertEquals(3, analyses.size());
    analyses = analysisRepository.query(2);
    assertEquals(1, analyses.size());
  }

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "newName", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
    Account user = em.find(Account.class, 1);
    Analysis result = analysisRepository.create(user, analysisCommand);
    assertTrue(analysisRepository.query(projectId).contains(result));
    assertEquals(null, result.getProblem());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    int analysisId = 1;
    Analysis analysis = analysisRepository.get(projectId, analysisId);
    assertEquals(em.find(Analysis.class, analysisId), analysis);
    assertEquals(null, analysis.getProblem());

    analysisId = 4;
    analysis = analysisRepository.get(projectId, analysisId);
    assertEquals(em.find(Analysis.class, analysisId), analysis);
    assertEquals("problem", analysis.getProblem());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    Analysis analysis = new Analysis(analysisId, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    Analysis updatedAnalysis = analysisRepository.update(user, analysis);
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
    Integer outcomeId = 1;
    Integer interventionId = 1;
    List<Outcome> selectedOutcomes = Arrays.asList(em.find(Outcome.class, outcomeId));
    List<Intervention> selectedInterventions = Arrays.asList(em.find(Intervention.class, interventionId));
    Analysis analysis = new Analysis(analysisId, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, selectedOutcomes, selectedInterventions);
    Analysis updatedAnalysis = analysisRepository.update(user, analysis);
    Analysis result = em.find(Analysis.class, analysisId);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
    assertEquals(analysis.getAnalysisType(), updatedAnalysis.getAnalysisType());
    assertEquals(analysis.getSelectedOutcomes(), updatedAnalysis.getSelectedOutcomes());
    assertEquals(updatedAnalysis, result);
  }

  @Test
  public void testUpdateWithInterventions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    Integer interventionId = 1;
    List<Intervention> selectedInterventions = Arrays.asList(em.find(Intervention.class, interventionId));
    Analysis analysis = new Analysis(analysisId, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST, selectedInterventions);
    Analysis updatedAnalysis = analysisRepository.update(user, analysis);
    Analysis result = em.find(Analysis.class, analysisId);
    assertEquals(analysis.getSelectedInterventions(), updatedAnalysis.getSelectedInterventions());
    assertEquals(updatedAnalysis, result);
  }

  @Test
  public void testupdateWithProblem() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    String problem = "problem";
    Account user = em.find(Account.class, 1);
    Analysis analysis = new Analysis(
      analysisId, 1, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST, Collections.EMPTY_LIST, problem);
    Analysis updatedAnalysis = analysisRepository.update(user, analysis);
    Analysis result = em.find(Analysis.class, analysisId);
    assertEquals(problem, updatedAnalysis.getProblem());
    assertEquals(updatedAnalysis, result);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    int projectId = 3;
    Outcome outcome1 = em.find(Outcome.class, 1);
    Outcome outcome2 = em.find(Outcome.class, 2);
    Analysis analysis = new Analysis(1, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Arrays.asList(outcome1, outcome2), Collections.EMPTY_LIST);
    analysisRepository.update(user, analysis);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateNotOwnedProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    int notOwnedProjectId = 2;
    Analysis analysis = new Analysis(notOwnedProjectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    analysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateAnalysisWithNonProjectOutcome() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 2);
    int projectId = 2;
    Outcome outcome2 = em.find(Outcome.class, 2);
    Analysis analysis = new Analysis(3, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Arrays.asList(outcome2), Collections.EMPTY_LIST);
    analysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateAnalysisWithNonProjectIntervention() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 2);
    int projectId = 2;
    Intervention intervention2 = em.find(Intervention.class, 2);
    Analysis analysis = new Analysis(3, projectId, "new name", AnalysisType.SINGLE_STUDY_BENEFIT_RISK, Collections.EMPTY_LIST, Arrays.asList(intervention2));
    analysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    analysisRepository.get(2, 1);
  }



}
