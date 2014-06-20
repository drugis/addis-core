package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    NetworkMetaAnalysis analysis = networkMetaAnalysisRepository.create(analysisCommand);
    assertNotNull(analysis);
    NetworkMetaAnalysis expectedAnalysis = em.find(NetworkMetaAnalysis.class, analysis.getId());
    assertEquals(expectedAnalysis, analysis);
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
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -6;
    Integer projectId = 1;
    Outcome outcome = em.find(Outcome.class, 1);
    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "new name", outcome);
    NetworkMetaAnalysis updatedAnalysis = networkMetaAnalysisRepository.update(analysis);
    assertEquals(analysis.getId(), updatedAnalysis.getId());
    assertEquals(analysis.getProjectId(), updatedAnalysis.getProjectId());
    assertEquals(analysis.getName(), updatedAnalysis.getName());
    assertEquals(outcome, updatedAnalysis.getOutcome());
  }

  @Test
  public void testUpdateExcludedArmsAndInterventions() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisId = -6;
    Integer projectId = 1;
    ArmExclusion newArmExclusion1 = new ArmExclusion(analysisId, -601L);
    ArmExclusion newArmExclusion2 = new ArmExclusion(analysisId, -602L);
    List<ArmExclusion> armExclusions = Arrays.asList(newArmExclusion1, newArmExclusion2);
    Outcome outcome = em.find(Outcome.class, 1);
    InterventionExclusion newInterventionExclusion = new InterventionExclusion(analysisId, 2);
    List<InterventionExclusion> interventionExclusions = Arrays.asList(newInterventionExclusion);

    NetworkMetaAnalysis analysis = new NetworkMetaAnalysis(analysisId, projectId, "new name", armExclusions, interventionExclusions, outcome);
    NetworkMetaAnalysis updatedAnalysis = networkMetaAnalysisRepository.update(analysis);
    assertEquals(2, updatedAnalysis.getExcludedArms().size());

    TypedQuery<ArmExclusion> query = em.createQuery("from ArmExclusion ae where ae.analysisId = :analysisId", ArmExclusion.class);
    query.setParameter("analysisId", analysisId);

    List<ArmExclusion> resultList = query.getResultList();
    assertEquals(2, resultList.size());
    assertEquals(new Integer(1), updatedAnalysis.getExcludedArms().get(0).getId());
    assertEquals(new Integer(2), updatedAnalysis.getExcludedArms().get(1).getId());
    assertEquals(new Integer(1), updatedAnalysis.getExcludedInterventions().get(0).getId());
  }


}
