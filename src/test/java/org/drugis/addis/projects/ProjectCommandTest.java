package org.drugis.addis.projects;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by daan on 3/12/14.
 */
public class ProjectCommandTest {
  @Test
  public void testGetDescription() throws Exception {
    ProjectCommand projectCommand = new ProjectCommand("name", null, 1);
    assertEquals(StringUtils.EMPTY, projectCommand.getDescription());
  }
}
