package org.drugis.addis.toggledColumns.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.toggledColumns.ToggledColumns;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
@Transactional
public class ToggledColumnsRepositoryTest {

  @Inject
  private ToggledColumnsRepository toggledColumnsRepository;

  private Integer workspaceId = -10;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGet() {
    ToggledColumns toggledColumns = toggledColumnsRepository.get(workspaceId);
    ToggledColumns expectedToggledColumns = new ToggledColumns(-10,
            "{criteria: true, units: true, description: false, references: false}");
    assertEquals(toggledColumns.getAnalysisId(),expectedToggledColumns.getAnalysisId());
    assertEquals(toggledColumns.getToggledColumns(),expectedToggledColumns.getToggledColumns());
  }

  @Test
  public void testPut(){
    toggledColumnsRepository.put(40,"some togglings");
    ToggledColumns result = toggledColumnsRepository.get(40);

    assertEquals(result.getAnalysisId(), new Integer(40));
    assertEquals(result.getToggledColumns(), "some togglings");
  }

}

