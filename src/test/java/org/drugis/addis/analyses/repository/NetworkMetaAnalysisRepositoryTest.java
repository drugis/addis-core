package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.model.AnalysisCommand;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class NetworkMetaAnalysisRepositoryTest {

  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Test
  public void testCreate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer projectId = 1;
    String title = "title";
    String type = "type";
    AnalysisCommand analysisCommand = new AnalysisCommand(projectId, title, type);

    NetworkMetaAnalysis networkMetaAnalysis = networkMetaAnalysisRepository.create(analysisCommand);
    NetworkMetaAnalysis expectedAnalysis = new NetworkMetaAnalysis(networkMetaAnalysis.getId(), projectId, title);
    assertEquals(expectedAnalysis, networkMetaAnalysis);
  }

  @Test
  public void testQueryByProjectId() {
    Integer projectId = 1;
    Collection<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByProjectId(projectId);
    Query query = em.createQuery("FROM NetworkMetaAnalysis WHERE id IN (-5, -6, -8)");
    List<NetworkMetaAnalysis> expectedAnalyses = query.getResultList();
    Set<NetworkMetaAnalysis> expectedSet = new HashSet<>(expectedAnalyses);
    Set<NetworkMetaAnalysis> resultSet = new HashSet<>(networkMetaAnalyses);
    assertEquals(expectedSet, resultSet);
  }

  @Test
  public void testQueryByOutcomes() {
    Integer outcomeId1 = 1;
    List<Integer> outcomIds = Collections.singletonList(outcomeId1);
    List<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByOutcomes(1, outcomIds);
    Query query = em.createQuery("FROM NetworkMetaAnalysis WHERE id IN (-5, -6, -8)");
    List<NetworkMetaAnalysis> expectedAnalyses = query.getResultList();
    Set<NetworkMetaAnalysis> expectedSet = new HashSet<>(expectedAnalyses);
    Set<NetworkMetaAnalysis> resultSet = new HashSet<>(networkMetaAnalyses);
    assertEquals(expectedSet, resultSet);
  }

  @Test
  public void testQueryByOutcomesWithoutIds() {
    List<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByOutcomes(1, null);
    assertEquals(Collections.emptyList(), networkMetaAnalyses);
  }

  @Test
  public void testUpdate() throws ResourceDoesNotExistException, MethodNotAllowedException {
    NetworkMetaAnalysis analysis = em.find(NetworkMetaAnalysis.class, -5);
    analysis.setTitle("test title");
    networkMetaAnalysisRepository.update(analysis);
    NetworkMetaAnalysis updatedAnalysis = em.find(NetworkMetaAnalysis.class, analysis.getId());
    assertEquals(analysis.getTitle(), updatedAnalysis.getTitle());
  }

  @Test
  public void testSetPrimaryModel() {
    Integer analysisId = -5;
    Integer modelId = 1;
    networkMetaAnalysisRepository.setPrimaryModel(analysisId, modelId);
    NetworkMetaAnalysis updatedAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(modelId, updatedAnalysis.getPrimaryModel());
  }

  @Test
  public void testSetTitle() {
    Integer analysisId = -5;
    String newTitle = "title";
    networkMetaAnalysisRepository.setTitle(analysisId, newTitle);
    NetworkMetaAnalysis updatedAnalysis = em.find(NetworkMetaAnalysis.class, analysisId);
    assertEquals(newTitle, updatedAnalysis.getTitle());
  }
}