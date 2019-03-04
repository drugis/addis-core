package org.drugis.addis.workspaceSettings.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.workspaceSettings.WorkspaceSettings;
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
public class WorkspaceSettingsRepositoryTest {

  @Inject
  private WorkspaceSettingsRepository workspaceSettingsRepository;

  private Integer workspaceId = -10;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGet() {
    WorkspaceSettings settings = workspaceSettingsRepository.get(workspaceId);
    WorkspaceSettings expectedSettings = new WorkspaceSettings(workspaceId,
            "{settings: {some: \"setting\"}, " +
                    "toggledColumns: {" +
                    "criteria: true, " +
                    "units: true, " +
                    "description: false, " +
                    "references: false}}");
    assertEquals(expectedSettings.getAnalysisId(), settings.getAnalysisId());
    assertEquals(expectedSettings.getSettings(), settings.getSettings());
  }

  @Test
  public void testPut() {
    workspaceSettingsRepository.put(10, "some settings");
    WorkspaceSettings result = workspaceSettingsRepository.get(10);
    assertEquals(new Integer(10), result.getAnalysisId());
    assertEquals("some settings", result.getSettings());
  }

}

