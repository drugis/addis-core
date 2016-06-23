package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
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
public class SingleStudyBenefitRiskAnalysisRepositoryTest {
  @Inject
  private SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Inject
  private AnalysisRepository analysisRepository;

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
    SingleStudyBenefitRiskAnalysis result = singleStudyBenefitRiskAnalysisRepository.create(analysisCommand);
    assertTrue(singleStudyBenefitRiskAnalysisRepository.query(projectId).contains(result));
    assertEquals(null, result.getProblem());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "analysis 1", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getTitle(), updatedAnalysis.getTitle());
    assertEquals(analysis.getSelectedOutcomes(), updatedAnalysis.getSelectedOutcomes());
  }

  @Test
  public void testUpdateWithOutcomes() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    Integer outcomeId = 1;
    Integer interventionId = -1;
    List<Outcome> selectedOutcomes = Arrays.asList(em.find(Outcome.class, outcomeId));
    InterventionInclusion interventionInclusion = new InterventionInclusion(analysisId, interventionId);
    em.persist(interventionInclusion);
    List<InterventionInclusion> interventionInclusions = Arrays.asList(interventionInclusion);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "analysis 1", selectedOutcomes, interventionInclusions);
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    SingleStudyBenefitRiskAnalysis result = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getTitle(), updatedAnalysis.getTitle());
    assertEquals(analysis.getSelectedOutcomes(), updatedAnalysis.getSelectedOutcomes());
    assertEquals(updatedAnalysis, result);
  }

  @Test
  public void testUpdateWithInterventions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -1;
    Account user = em.find(Account.class, 1);
    Integer projectId = 1;
    Integer interventionId = -1;
    InterventionInclusion inc = new InterventionInclusion(analysisId, interventionId);
    em.persist(inc);
    List<InterventionInclusion> interventionInclusions = Arrays.asList(inc);
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "new name", Collections.emptyList(),
            interventionInclusions);
    em.merge(analysis);
    SingleStudyBenefitRiskAnalysis result = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    assertEquals(analysis.getInterventionInclusions(), analysis.getInterventionInclusions());
    assertEquals(analysis, result);
  }

  @Test
  public void testupdateWithProblem() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -1;
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
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(-1, projectId, "new name", Arrays.asList(outcome1, outcome2), Collections.EMPTY_LIST);
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
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(-3, projectId, "new name", Arrays.asList(outcome2), Collections.EMPTY_LIST);
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testUpdateAnalysisWithNonProjectIntervention() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = em.find(Account.class, 2);
    int analysisId = -3;
    int projectId = 2;
    int interventionId = -2;
    List<InterventionInclusion> interventionInclusions = Arrays.asList(new InterventionInclusion(analysisId, interventionId));
    SingleStudyBenefitRiskAnalysis analysis = new SingleStudyBenefitRiskAnalysis(analysisId, projectId, "new name", Collections.EMPTY_LIST, interventionInclusions);
    singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
  }

}
