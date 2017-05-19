package org.drugis.addis.scaledUnits.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.scaledUnits.ScaledUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by joris on 19-4-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ScaledUnitRepositoryTest {
  @Inject
  private ScaledUnitRepository scaledUnitRepository;
  private Integer projectId = 1;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testQuery() {
    List<ScaledUnit> result = scaledUnitRepository.query(projectId);
    ScaledUnit unit = new ScaledUnit(10, projectId,
            URI.create("http://concept10.bla"), 0.1, "gram");
    assertEquals(unit, result.get(0));
  }

  @Test
  public void testCreate() {
    ScaledUnit unit = new ScaledUnit(21, projectId, URI.create("http://concept10.bla"), 0.01, "gram");
    scaledUnitRepository.create(unit.getProjectId(), unit.getConceptUri(), unit.getMultiplier(), unit.getName());
    List<ScaledUnit> result = scaledUnitRepository.query(projectId);
    assertEquals(unit, result.get(1));
  }
}