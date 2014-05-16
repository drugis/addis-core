package org.drugis.addis.trialverse;

import org.drugis.addis.config.JpaTrialverseRepositoryTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by connor on 2/26/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaTrialverseRepositoryTestConfig.class})
@Transactional
public class TrialverseRepositoryTest {
  @Inject
  private TrialverseRepository trialverseRepository;

  @PersistenceContext(unitName = "trialverse")
  EntityManager em;

  @Test
  public void testQuery() {
    Collection<Namespace> namespaces = trialverseRepository.query();
    assertEquals(3, namespaces.size());
  }

  @Test
  public void testGet() throws ResourceDoesNotExistException {
    Namespace namespace = trialverseRepository.get(1L);
    assertEquals(new Long(1), namespace.getId());
  }

  @Test
  public void testQueryStudy() {
    Long namespaceId = 1L;
    Long studyId = 1L;
    Study studyInNamespace = em.find(Study.class, studyId);
    List<Study> studyList = trialverseRepository.queryStudies(namespaceId);
    assertTrue(studyList.contains(studyInNamespace));
    assertEquals(3, studyList.size());
    Long studyId2 = 4L;
    Study studyNotInNamespace = em.find(Study.class, studyId2);
    assertFalse(studyList.contains(studyNotInNamespace));
  }

  @Test
  public void testGetStudiesByIds() {
    Long namespaceId = 1L;
    Long studyId1 = 1L;
    Long studyId2 = 2L;
    Long studyId3 = 3L;
    Study study1InNamespace = em.find(Study.class, studyId1);
    Study study2InNamespace = em.find(Study.class, studyId1);
    Study study3InNamespace = em.find(Study.class, studyId1);
    List<Study> studiesInNamespace = Arrays.asList(study1InNamespace, study2InNamespace, study3InNamespace);
    List<Long> studyIds = Arrays.asList(studyId1, studyId2, studyId3);
    List<Study> studyList = trialverseRepository.getStudiesByIds(namespaceId, studyIds);
    assertTrue(studyList.containsAll(studiesInNamespace));
    assertEquals(3, studyList.size());
    Long studyId4 = 4L;
    Study studyNotInNamespace = em.find(Study.class, studyId4);
    assertFalse(studyList.contains(studyNotInNamespace));
  }

  @Test
  public void testGetArmNamesByDrugIds() {
    Integer studyId = 1;
    List<Long> drugIds = Arrays.asList(1L, 2L, 3L);
    List<Arm> result = trialverseRepository.getArmsByDrugIds(studyId, drugIds);
    assertEquals(2, result.size());
    Arm arm = em.find(Arm.class, 1L);
    assertTrue(result.contains(arm));
  }

  @Test
  public void testGetVariableNamesByOutcomeIds() {
    Variable expected = em.find(Variable.class, 1L);
    List<Long> outcomeIds = Arrays.asList(1L, 2L, 3L);
    List<Variable> result = trialverseRepository.getVariablesByOutcomeIds(outcomeIds);
    assertEquals(2, result.size());
    assertTrue(result.contains(expected));
  }

  @Test
  public void testGetOrderedMeasurements() {
    List<Long> outcomeIds = Arrays.asList(1L, 2L);
    List<Long> armIds = Arrays.asList(1L);
    List<Measurement> result = trialverseRepository.getOrderedMeasurements(outcomeIds, armIds);
    assertEquals(4, result.size());
  }

  @Test
  public void testGetOrderedMeasurementsTestForMeasureAttribute() {
    List<Long> outcomeIds = Arrays.asList(2L);
    List<Long> armIds = Arrays.asList(1L);
    List<Measurement> result = trialverseRepository.getOrderedMeasurements(outcomeIds, armIds);
    assertEquals(2, result.size());
    boolean foundStandardDeviationType = false, foundMeanType = false;
    for (Measurement measurement : result) {
      if (measurement.getMeasurementKey().getMeasurementAttribute() == MeasurementAttribute.STANDARD_DEVIATION) {
        foundStandardDeviationType = true;
      }
      if (measurement.getMeasurementKey().getMeasurementAttribute() == MeasurementAttribute.MEAN) {
        foundMeanType = true;
      }
    }
    assertTrue(foundMeanType);
    assertTrue(foundStandardDeviationType);
  }

  @Test
  public void testEmptyOutcomeIdInput() {
    List<Variable> result = trialverseRepository.getVariablesByOutcomeIds(new ArrayList<Long>());
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetArmsForStudies() {
    Long namespaceId = 1L;
    List<Long> studyIds = Arrays.asList(1L, 2L);
    List<Variable> variables = Arrays.asList(em.find(Variable.class, 1L), em.find(Variable.class, 2L));
    List<TrialDataArm> arms = trialverseRepository.getArmsForStudies(namespaceId, studyIds, variables);
    assertEquals(2, arms.size());
  }

}
