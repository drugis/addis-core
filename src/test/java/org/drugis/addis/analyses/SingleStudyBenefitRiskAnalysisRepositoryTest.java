package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
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
public class SingleStudyBenefitRiskAnalysisRepositoryTest {
  @Inject
  private SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testQuery() {
    Collection<SingleStudyBenefitRiskAnalysis> analyses = singleStudyBenefitRiskAnalysisRepository.query(1);
    assertEquals(3, analyses.size());
    analyses = singleStudyBenefitRiskAnalysisRepository.query(2);
    assertEquals(1, analyses.size());
  }

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, "newName", AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
    Account user = em.find(Account.class, 1);
    SingleStudyBenefitRiskAnalysis result = singleStudyBenefitRiskAnalysisRepository.create(user, analysisCommand);
    assertTrue(singleStudyBenefitRiskAnalysisRepository.query(projectId).contains(result));
    assertEquals(null, result.getProblem());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException, MethodNotAllowedException {
    int projectId = 1;
    int analysisId = 1;
    SingleStudyBenefitRiskAnalysis analysis = singleStudyBenefitRiskAnalysisRepository.get(projectId, analysisId);
    assertEquals(em.find(SingleStudyBenefitRiskAnalysis.class, analysisId), analysis);
    assertEquals(null, analysis.getProblem());

    analysisId = 4;
    analysis = singleStudyBenefitRiskAnalysisRepository.get(projectId, analysisId);
    assertEquals(em.find(SingleStudyBenefitRiskAnalysis.class, analysisId), analysis);
    assertEquals("problem", analysis.getProblem());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "new name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
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
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "new name", selectedOutcomes, selectedInterventions);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    SingleStudyBenefitRiskAnalysis result = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
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
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "new name", Collections.EMPTY_LIST, selectedInterventions);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    SingleStudyBenefitRiskAnalysis result = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(analysis.getSelectedInterventions(), updatedAnalysis.getSelectedInterventions());
    assertEquals(updatedAnalysis, result);
  }

  @Test
  public void testupdateWithProblem() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = 1;
    String problem = "problem";
    Account user = em.find(Account.class, 1);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(
            analysisId, 1, "new name", Collections.EMPTY_LIST, Collections.EMPTY_LIST, problem);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    SingleStudyBenefitRiskAnalysis result = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(problem, updatedAnalysis.getProblem());
    assertEquals(updatedAnalysis, result);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateInWrongProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    int projectId = 3;
    Outcome outcome1 = em.find(Outcome.class, 1);
    Outcome outcome2 = em.find(Outcome.class, 2);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(1, projectId, "new name", Arrays.asList(outcome1, outcome2), Collections.EMPTY_LIST);
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateNotOwnedProjectFails() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 1);
    int notOwnedProjectId = 2;
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(notOwnedProjectId, "new name", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateAnalysisWithNonProjectOutcome() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 2);
    int projectId = 2;
    Outcome outcome2 = em.find(Outcome.class, 2);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(3, projectId, "new name", Arrays.asList(outcome2), Collections.EMPTY_LIST);
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateAnalysisWithNonProjectIntervention() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 2);
    int projectId = 2;
    Intervention intervention2 = em.find(Intervention.class, 2);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(3, projectId, "new name", Collections.EMPTY_LIST, Arrays.asList(intervention2));
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testGetFromWrongProjectFails() throws ResourceDoesNotExistException {
    singleStudyBenefitRiskAnalysisRepository.get(2, 1);
  }


}
