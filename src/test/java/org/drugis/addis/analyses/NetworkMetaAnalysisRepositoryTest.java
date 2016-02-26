package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by daan on 7-5-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    NetworkMetaAnalysis analysis = networkMetaAnalysisRepository.create(analysisCommand);
    assertNotNull(analysis);
    NetworkMetaAnalysis expectedAnalysis = em.find(NetworkMetaAnalysis.class, analysis.getId());
    assertEquals(expectedAnalysis, analysis);
    assertEquals(2, analysis.getIncludedInterventions().size());
    assertEquals(2, expectedAnalysis.getIncludedInterventions().size());
  }

  @Test
  public void testQuery() {
    Integer projectId = 1;
    Integer analysisId = -5;
    Collection<NetworkMetaAnalysis> result = networkMetaAnalysisRepository.query(projectId);
    NetworkMetaAnalysis expectedAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertTrue(result.contains(expectedAnalysis));
    assertEquals(2, result.size());
  }

  @Test
  public void testQueryByOutcomes() {
    Integer projectId = 1;
    Integer analysisId = -6;
    List<Integer> outcomeIds= Arrays.asList(1);
    Collection<NetworkMetaAnalysis> result = networkMetaAnalysisRepository.queryByOutcomes(projectId, outcomeIds);
    NetworkMetaAnalysis expectedAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertTrue(result.contains(expectedAnalysis));
    assertEquals(1, result.size());
    assertEquals((Integer) 1, new ArrayList<>(result).get(0).getOutcome().getId());
  }

  @Test
  public void testQueryByOutcomesEmptyList() {
    Integer projectId = 1;
    Collection<NetworkMetaAnalysis> result = networkMetaAnalysisRepository.queryByOutcomes(projectId, Collections.emptyList());
    assertEquals(0, result.size());
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -6;
    Integer projectId = 1;
    Outcome outcome = em.find(Outcome.class, 1);
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "new name", outcome);
    NetworkMetaAnalysis updatedAnalysis = networkMetaAnalysisRepository.update(analysis);
    assertEquals(analysis.getId(), updatedAnalysis.getId());
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getTitle(), updatedAnalysis.getTitle());
    assertEquals(outcome, updatedAnalysis.getOutcome());
  }

  @Test
  public void testUpdateExcludedArmsAndInterventions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -6;
    NetworkMetaAnalysis analysis = em.find(NetworkMetaAnalysis.class, analysisId);

    ArmExclusion newArmExclusion1 = new ArmExclusion(analysis, "-601L");
    ArmExclusion newArmExclusion2 = new ArmExclusion(analysis, "-602L");
    analysis.getExcludedArms().clear();
    analysis.getExcludedArms().add(newArmExclusion1);
    analysis.getExcludedArms().add(newArmExclusion2);

    int interventionId = 2;
    InterventionInclusion newInterventionInclusion = new InterventionInclusion(analysis, interventionId);

    analysis.getIncludedInterventions().clear();
    analysis.getIncludedInterventions().add(newInterventionInclusion);

    NetworkMetaAnalysis updatedAnalysis = networkMetaAnalysisRepository.update(analysis);
    assertEquals(2, updatedAnalysis.getExcludedArms().size());

    TypedQuery<ArmExclusion> query = em.createQuery("from ArmExclusion ae where ae.analysis.id = :analysisId", ArmExclusion.class);
    query.setParameter("analysisId", analysisId);
    List<ArmExclusion> resultList = query.getResultList();

    TypedQuery<InterventionInclusion> query2 = em.createQuery("from InterventionInclusion ii where ii.analysis.id = :analysisId", InterventionInclusion.class);
    query2.setParameter("analysisId", analysisId);

    assertEquals(2, resultList.size());
    assertEquals(new Integer(1), updatedAnalysis.getExcludedArms().get(0).getId());
    assertEquals(new Integer(2), updatedAnalysis.getExcludedArms().get(1).getId());
    assertEquals(new Integer(1), updatedAnalysis.getIncludedInterventions().get(0).getId());
    assertEquals(new Integer(interventionId), updatedAnalysis.getIncludedInterventions().get(0).getInterventionId());
  }

  @Test
  public void testSetPrimary() {
    int analysisId = -7;
    int modelId = 2;
    NetworkMetaAnalysis previouslyPrimary = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(1, (int)previouslyPrimary.getPrimaryModel());
    networkMetaAnalysisRepository.setPrimaryModel(analysisId, modelId);
    NetworkMetaAnalysis newPrimary = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(modelId, (int)newPrimary.getPrimaryModel());
  }

}
